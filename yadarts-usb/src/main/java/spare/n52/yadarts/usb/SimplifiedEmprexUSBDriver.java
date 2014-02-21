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
import java.util.List;

import javax.usb.UsbConst;
import javax.usb.UsbDevice;
import javax.usb.UsbDisconnectedException;
import javax.usb.UsbEndpoint;
import javax.usb.UsbException;
import javax.usb.UsbHostManager;
import javax.usb.UsbHub;
import javax.usb.UsbInterface;
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
public class SimplifiedEmprexUSBDriver {

	private static final Logger logger = LoggerFactory
			.getLogger(SimplifiedEmprexUSBDriver.class);
	private static short defaultVendorId = (short) Integer.valueOf("046e", 16)
			.intValue();
	private static short defaultProductId = (short) Integer.valueOf("d300", 16)
			.intValue();

	private short targetVendor;
	private short targetProduct;
	private EmprexCommunicationThread driverThread;
	private EmprexRawDataListener listener;
	private UsbPipe pipe;

	public SimplifiedEmprexUSBDriver(EmprexRawDataListener el) throws IOException {
		this(defaultVendorId, defaultProductId, el);
	}

	public SimplifiedEmprexUSBDriver(short defaultVendorId2, short defaultProductId2,
			EmprexRawDataListener el) throws IOException {
		this.targetVendor = defaultVendorId2;
		this.targetProduct = defaultProductId2;
		this.listener = el;

		try {
			initialize();
		} catch (SecurityException | UsbException e) {
			throw new IOException(e);
		}
	}

	public boolean isReady() {
		return this.pipe != null;
	}

	protected void initialize() throws SecurityException, UsbException {
		UsbServices services = UsbHostManager.getUsbServices();
		UsbHub usbHub = services.getRootUsbHub();

		UsbDevice theDevice = findDevice(usbHub, targetVendor, targetProduct);

		if (theDevice == null) {
			logger.warn("Could not find the device. The driver is not operable.");
			return;
		}
		
		for (Object i : theDevice.getActiveUsbConfiguration().getUsbInterfaces()) {
			UsbInterface intf = (UsbInterface) i;
			for (Object e : intf.getUsbEndpoints()) {
				UsbEndpoint endp = (UsbEndpoint) e;
				if (endp.getDirection() == UsbConst.ENDPOINT_DIRECTION_IN) {
					this.pipe = endp.getUsbPipe();
				}
			}
		}
	}

	private UsbDevice findDevice(UsbHub usbHub, short defaultVendorId2, short defaultProductId2) {
		UsbDevice result = null;
		for (Object d : usbHub.getAttachedUsbDevices()) {
			if (d instanceof UsbHub) {
				result = findDevice((UsbHub) d, defaultVendorId2, defaultProductId2);
				if (result != null) {
					return result;
				}
			}
			else if (d instanceof UsbDevice) {
				UsbDevice device = (UsbDevice) d;
				if (device.getUsbDeviceDescriptor().idProduct() == defaultProductId2
						&& device.getUsbDeviceDescriptor().idVendor() == defaultVendorId2) {
					return device;
				}
			}
		}
		return null;
	}

	public void start() throws IOException {
		try {
			this.pipe.getUsbEndpoint().getUsbInterface().claim(new UsbInterfacePolicy() {
				@Override
				public boolean forceClaim(UsbInterface usbInterface) {
					return true;
				}
			});
		} catch (UsbException e) {
			logger.warn(e.getMessage(), e);
			throw new IOException(e);
		}

		try {
			pipe.open();
		} catch (UsbException e) {
			try {
				this.pipe.getUsbEndpoint().getUsbInterface().release();
			} catch (UsbException e1) {
				logger.warn(e1.getMessage(), e1);
			}
			throw new IOException(
					"Could not open the desired endpoint of the device", e);
		}

		driverThread = new EmprexCommunicationThread();
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
		if (driverThread == null) {
			return;
		}
		try {
			driverThread.shutdown();
			
			if (this.pipe != null && this.pipe.getUsbEndpoint() != null
					&& this.pipe.getUsbEndpoint().getUsbInterface() != null) {
				this.pipe.getUsbEndpoint().getUsbInterface().release();	
			}
			
		} catch (UsbNotActiveException | UsbNotOpenException
				| UsbDisconnectedException | UsbException e) {
			throw new IOException(e);
		}
	}


	/**
	 * The ever-living thread, receiving the raw data from the device.
	 */
	public class EmprexCommunicationThread implements Runnable {

		private boolean running = true;

		public EmprexCommunicationThread() {
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
					if (pipe.isOpen() && pipe.isActive()) {
						byteCount = pipe.syncSubmit(buffer);
					}
				} catch (UsbException e) {
					/*
					 * a timeout error occurs when no data has been
					 * received in the specified timeout
					 */
					if (!e.getMessage().contains("LIBUSB_ERROR_TIMEOUT")) {
						logger.warn("Exception in Usb communication: {}",
								e.getMessage());	
					}
					
					if (running) {
						continue;
					}
				} catch (RuntimeException e) {
					logger.warn(e.getMessage());
					if (running) {
						logger.info("Restarting USB handler thread.");
						restartConnection();
					}
				}

				if (running) {
					SimplifiedEmprexUSBDriver.this.listener
							.receiveData(buffer, byteCount);
				}
			}
		}

		private synchronized void restartConnection() {
			try {
				SimplifiedEmprexUSBDriver.this.shutdown();
			} catch (IOException e) {
				logger.warn(e.getMessage());
			}
			
			try {
				SimplifiedEmprexUSBDriver.this.start();
			} catch (IOException e) {
				logger.warn(e.getMessage());
			}
		}

		public synchronized void shutdown() throws UsbException {
			running = false;
			if (pipe.isOpen()) {
				pipe.abortAllSubmissions();
				pipe.close();
			}
			
		}

	}
}
