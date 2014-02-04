# yadarts - yet another darts software

This open source project uses the Emprex Wireless Dart Board and its
protocol as the basis for a sophisticated, extensible dart software.

## USB setup

Depending on the configuration and the OS of your machine, a few
steps might be required to be able to access the USB dongle.

### Linux

You require write permission to the device file of the USB dongle. If
the program works when executed as `root` you may configure `udev`
to permit write access to the device. Store the following snippet
in a file like `/lib/udev/rules.d/99-emprexdevice.rules`.

```Shell
SUBSYSTEM=="usb",ATTR{idVendor}=="046e",ATTR{idProduct}=="d300",MODE="0660",GROUP="plugdev"
```

`idVendor` and `idProduct` are the values observed with the dongles
available to the project. If this does not work, try identifying the
correct values via [usb-devices](http://linux.die.net/man/1/usb-devices).
Feedback very welcome!

### Windows

tbd

