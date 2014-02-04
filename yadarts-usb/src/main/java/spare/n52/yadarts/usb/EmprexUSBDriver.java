/**
 * Copyright 2014 the staff of 52°North Initiative for Geospatial Open
 * Source Software GmbH in their free time
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package spare.n52.yadarts.usb;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.usb.UsbConst;
import javax.usb.UsbControlIrp;
import javax.usb.UsbDevice;
import javax.usb.UsbDisconnectedException;
import javax.usb.UsbEndpoint;
import javax.usb.UsbException;
import javax.usb.UsbHostManager;
import javax.usb.UsbHub;
import javax.usb.UsbInterface;
import javax.usb.UsbInterfaceDescriptor;
import javax.usb.UsbInterfacePolicy;
import javax.usb.UsbNotActiveException;
import javax.usb.UsbNotOpenException;
import javax.usb.UsbPipe;
import javax.usb.UsbServices;
import javax.usb.util.UsbUtil;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This is the main class for low-level communication with the Emprex HID
 * dongle.
 */
public class EmprexUSBDriver {

	private static final Logger logger = LoggerFactory
			.getLogger(EmprexUSBDriver.class);
	private static short defaultVendorId = (short) Integer.valueOf("046e", 16)
			.intValue();
	private static short defaultProductId = (short) Integer.valueOf("d300", 16)
			.intValue();

	private UsbInterface theInterface;
	private short vendor;
	private short product;
	private EmprexCommunicationThread driverThread;
	private EmprexRawDataListener listener;

	public EmprexUSBDriver(EmprexRawDataListener el) throws IOException {
		this(defaultVendorId, defaultProductId, el);
	}

	public EmprexUSBDriver(short defaultVendorId2, short defaultProductId2,
			EmprexRawDataListener el) throws IOException {
		this.vendor = defaultVendorId2;
		this.product = defaultProductId2;
		this.listener = el;

		try {
			initialize();
		} catch (SecurityException | UsbException e) {
			throw new IOException(e);
		}
	}

	public boolean isReady() {
		return this.theInterface != null;
	}

	private void initialize() throws SecurityException, UsbException {
		UsbServices services = UsbHostManager.getUsbServices();
		UsbHub usbHub = services.getRootUsbHub();

		List<UsbInterface> usbInterfaces = findInterfacesImplementations(
				usbHub, Constants.HID_DEVICE_CLASS);

		logger.info("There are {} HID interfaces available",
				usbInterfaces.size());

		for (int i = 0; i < usbInterfaces.size(); i++) {
			UsbInterface usbInterface = usbInterfaces.get(i);

			if (!assertClassAndProtocol(usbInterface)) {
				continue;
			}

			if (!assertIdsAndUsage(usbInterface, this.vendor, this.product)) {
				continue;
			}

			/*
			 * both fitted
			 */
			logger.info("Found the device: {}", usbInterface);
			this.theInterface = usbInterface;
		}

	}

	public void start() throws IOException {
		try {
			theInterface.claim(new UsbInterfacePolicy() {
				@Override
				public boolean forceClaim(UsbInterface usbInterface) {
					return true;
				}
			});
		} catch (UsbException e) {
			logger.warn(e.getMessage(), e);
			throw new IOException(e);
		}

		UsbEndpoint theEndpoint = findInterruptInEndpoint(theInterface
				.getUsbEndpoints());

		if (null == theEndpoint) {
			throw new IOException(
					"Could not find the interrupt-in endpoint of the device");
		}

		UsbPipe pipe = theEndpoint.getUsbPipe();

		try {
			pipe.open();
		} catch (UsbException e) {
			try {
				theInterface.release();
			} catch (UsbException e1) {
				logger.warn(e1.getMessage(), e1);
			}
			throw new IOException(
					"Could not open the desired endpoint of the device", e);
		}

		driverThread = new EmprexCommunicationThread(pipe);
		Thread t = new Thread(driverThread);
		t.start();
	}

	protected UsbEndpoint findInterruptInEndpoint(List<?> usbEndpoints) {
		for (int i = 0; i < usbEndpoints.size(); i++) {
			UsbEndpoint usbEndpoint = (UsbEndpoint) usbEndpoints.get(i);

			if (UsbConst.ENDPOINT_TYPE_INTERRUPT == usbEndpoint.getType()
					&& UsbConst.ENDPOINT_DIRECTION_IN == usbEndpoint
							.getDirection()) {
				return usbEndpoint;
			}
		}

		return null;
	}

	public void shutdown() throws IOException {
		try {
			driverThread.setRunning(false);
			theInterface.release();
		} catch (UsbNotActiveException | UsbNotOpenException
				| UsbDisconnectedException | UsbException e) {
			throw new IOException(e);
		}
	}

	private boolean assertClassAndProtocol(UsbInterface usbInterface) {
		UsbInterfaceDescriptor desc = usbInterface.getUsbInterfaceDescriptor();

		if (desc.bInterfaceSubClass() == 0x01
				&& desc.bInterfaceProtocol() == 0x02) {
			/*
			 * the device must implement the "boot" interface
			 */
			return true;
		}

		return false;
	}

	private boolean assertIdsAndUsage(UsbInterface usbInterface,
			short vendorId, short productId) {
		UsbDevice usbDevice = usbInterface.getUsbConfiguration().getUsbDevice();

		if (!(usbDevice.getUsbDeviceDescriptor().idProduct() == productId && usbDevice
				.getUsbDeviceDescriptor().idVendor() == vendorId)) {
			/*
			 * check if its the desired device
			 */
			return false;
		}
		
		try {
			usbInterface.claim(new UsbInterfacePolicy() {
				@Override
				public boolean forceClaim(UsbInterface usbInterface) {
					return true;
				}
			});
		} catch (UsbNotActiveException
				| UsbDisconnectedException | UsbException e1) {
			logger.warn(e1.getMessage(), e1);
			return false;
		}

		UsbControlIrp usageRequest = usbDevice.createUsbControlIrp(
				Constants.GET_DESCRIPTOR_REQUESTTYPE,
				UsbConst.REQUEST_GET_DESCRIPTOR,
				(short) (Constants.HID_DESCRIPTOR << 8), UsbUtil
						.unsignedShort(usbInterface.getUsbInterfaceDescriptor()
								.bInterfaceNumber()));

		byte[] data = new byte[64];
		usageRequest.setData(data);

		try {
			/*
			 * push the Get Report request
			 */
			usbDevice.syncSubmit(usageRequest);

			return parseUsageResponse(usageRequest, data);
		} catch (UsbException e) {
			logger.warn(e.getMessage());
		} finally {
			try {
				usbInterface.release();
			} catch (UsbException e) {
				logger.warn(e.getMessage(), e);
			}
		}

		return false;
	}

	protected boolean parseUsageResponse(UsbControlIrp usageRequest, byte[] data) {
		if (4 > usageRequest.getActualLength()) {
			/*
			 * as usage Irp should return at least 4 bytes
			 */
			return false;
		}

		if (UsbUtil.toInt(Constants.HID_USAGE_PAGE, Constants.HID_USAGE_ID) == 
				UsbUtil.toInt(data[0], data[1], data[2], data[3])) {
			/*
			 * the usage fits!
			 */
			return true;
		}

		return false;
	}

	private List<UsbInterface> findInterfacesImplementations(
			UsbDevice theDevice, byte clazz) {
		List<UsbInterface> list = new ArrayList<>();

		if (theDevice.isConfigured()) {
			List<?> ifaces = theDevice.getActiveUsbConfiguration()
					.getUsbInterfaces();

			for (int i = 0; i < ifaces.size(); i++) {
				UsbInterface usbInterface = (UsbInterface) ifaces.get(i);

				if (usbInterface.getUsbInterfaceDescriptor()
						.bInterfaceClass() == clazz) {
					list.add(usbInterface);
				}
			}
		}

		if (theDevice.isUsbHub()) {
			List<?> devices = ((UsbHub) theDevice).getAttachedUsbDevices();
			for (int i = 0; i < devices.size(); i++) {
				list.addAll(findInterfacesImplementations(
						(UsbDevice) devices.get(i), clazz));
			}
		}

		return list;
	}

	/**
	 * The ever-living thread, receiving the raw data from the device.
	 */
	public class EmprexCommunicationThread implements Runnable {

		private boolean running = true;
		private UsbPipe pipe = null;

		public EmprexCommunicationThread(UsbPipe pipe) {
			this.pipe = pipe;
		}

		public void run() {
			/*
			 * define a buffer with the max packet size of the endpoint
			 */
			byte[] buffer = new byte[UsbUtil.unsignedInt(pipe.getUsbEndpoint()
					.getUsbEndpointDescriptor().wMaxPacketSize())];

			int byteCount = 0;
			while (running) {
				try {
					/*
					 * blocking call!
					 */
					byteCount = pipe.syncSubmit(buffer);
				} catch (UsbException e) {
					logger.debug("Exception in Usb communication: {}",
							e.getMessage());
					if (running) {
						continue;
					}
				}

				if (running) {
					EmprexUSBDriver.this.listener
							.receiveData(buffer, byteCount);
				}
			}
		}

		public void setRunning(boolean r) throws UsbException {
			running = r;
			if (!r) {
				pipe.abortAllSubmissions();
				this.pipe.close();
			}
		}

	}
}
