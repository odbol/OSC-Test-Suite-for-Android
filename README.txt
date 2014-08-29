OSC Test Suite for Android
by odbol
http://www.odbol.com

This is an example app that listens for OSC messages (i.e. an OSC server). 

You can use the OSCSampleServer example to write your own hooks for OSC messages in your app.

It also includes the OSCTesterClientService, which is a service that runs in the background and
sends test OSC messages, so you can make sure your app is receiving them as expected. In a real app,
you do not need to start this service - we will assume that the user will start their own OSC client
either as a service on the phone or on a separate device (e.g. a laptop connected via WiFi).

It may not seem exciting by itself, but opening your app up with OSC support will allow users to
control your app using more than just the screen on the phone: you could use a remote device such
as a laptop, an OSC-enabled controller such as the Monome, or even a standard MIDI keyboard using
software such as Sensorizer ( http://sensorizer.com ) or OSCulator to convert MIDI to OSC. 

The most exciting method, however, is using the DrumPants Android app (coming soon!) coupled with
an Arduino microcontroller to enable controlling your phone using physical sensors hooked up via 
Bluetooth, such as DrumPants ( http://drumpants.com ). This
will pave the way towards complete mobile performance interfaces, allowing people to perform music
and/or video using nothing more than their phone and the clothes on their body!

INSTALL:
================

It uses the OSCLib library from:

https://github.com/odbol/OSCLib

Please download the library and add it to your build path.

KNOWN BUGS:
===============

If you start listening AFTER you have started the OSC Client service, the OSC server does not 
receive new messages. I think this is due to the OscClient.sessionClosed() function not actually
throwing away the closed session. It will need code to restart a session when this happens.