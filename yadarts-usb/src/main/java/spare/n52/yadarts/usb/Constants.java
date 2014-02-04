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

import javax.usb.UsbConst;

/**
 * Usb constants. Basically this static class provides
 * constant bytes which define a certain request(type)
 * conforming to the USB spec.
 */
public class Constants {

	/**
	 * The bInterfaceClass as for a HID device as provides by a interface descriptor
	 */
	public static final byte HID_DEVICE_CLASS = 0x03;
	
	/**
	 * the usage page as specified for a HID device
	 */
	public static final short HID_USAGE_PAGE = 0x0501;
	
	/**
	 * the usage id as specified for a HID device
	 */
	public static final short HID_USAGE_ID = 0x0902;

	/**
	 * the wValue for a HID descriptor type request
	 */
	public static final byte HID_DESCRIPTOR = 0x22;

	/**
	 * the type definition for a get descriptor request
	 */
	public static final byte GET_DESCRIPTOR_REQUESTTYPE = UsbConst.REQUESTTYPE_TYPE_STANDARD
			| UsbConst.REQUESTTYPE_RECIPIENT_INTERFACE
			| UsbConst.REQUESTTYPE_DIRECTION_IN;
	
}
