All code is provided without any warranty, guarantee or license.  Have at it!


Course:

ECE1778 - Creative Applications for Mobile Devices
Professor Jonathan Rose
University of Toronto
Winter 2013


Team:

René Rail-Ip
Paul Grouchy
Hao Yan


Logo:

Sebastian Koever
http://www.sebastiankoever.com/


Materials:

Galaxy Nexus | Android 4.2.1
AX12A CrustCrawler SmartArm


Original Source Code:

https://github.com/rodericus1987/roboArmApp


Resources:

1. Coordinates Transformation of acceleration:
a. http://stackoverflow.com/questions/14963190/calculate-acceleration-inreference-to-true-north/14988559#14988559

2. Issues surrounding multiple ASyncTasks:
a. http://foo.jasonhudgins.com/2010/05/limitations-of-asynctask.html
b. http://stackoverflow.com/questions/11241600/async-task-doinbackground-notperformed
c. http://stackoverflow.com/questions/4068984/running-multiple-asynctasks-atthe-same-time-not-possible

3. Dynamixel API for Controlling AX12A Motors:
a. http://support.robotis.com/en/software/dynamixel_sdk/api_reference.htm

4. Java TCP/IP Server Implementation in MATLAB:
a. http://iheartmatlab.blogspot.ca/2008/08/tcpip-socket-communications-in-matlab.html


Excerpts from Project Final Report:

There are three major components to the A.R.M. application: the app on the mobile device, the host computer running MATLAB and the AX12A Dynamixel robotic arm. On the mobile device side, the A.R.M. app collects the movements of the device by the end user, but only when the user is holding down the main button. The app then translates the device’s movement outputs into an absolute, real-world coordinate frame using the device’s magnetometer and rotation sensors. It is at this point that various noise-reduction steps are implemented. There are three techniques used: thresholding, calibration, and speed decay. Thresholding is used to ignore small acceleration values, calibration subtracts average accelerometer values taken from when the device was calibrated at rest by the end-user, and speed decay subtracts a fixed value per second from the speed to counteract the accumulation of integration errors (device displacement is calculated by integrating accelerometer data to get speed and then integrating speed to get displacement and accelerometer errors are accumulated during these steps). Also, it is at this point that displacement values are scaled by a user-defined sensitivity value. Relative device displacement values are periodically sent via Wi-Fi to the host computer. If the user is not holding down the main button, zeros are sent. The current gripper slider bar value is also sent with this data. If an axis’ movement is locked by the user or by the Wrist Mode/Arm Mode functionality, a zero is sent for that movement. If a periodic data send fails, the app assumes the Wi-Fi connection has been lost. The mobile app was set up for intuitive usability, with the majority of the user interactions happening through the main button.

On the host computer side, relative device movements are received from the device over Wi-Fi and are used to compute (using inverse kinematics) the motor signals required to move the robotic arm’s end effector in a manner that mimics the mobile device’s movements. The custom code running in MATLAB must first detect whether the device is sending Wrist Mode or Arm Mode data by examining which data values are zeros, and then use the non-zero data to calculate the specified arm motor movements. The gripper is adjusted if the gripper slider value has changed. The MATLAB code also detects when requested movements are outside of the robotic arm’s motor ranges, in which case the mobile device is notified via a signal over Wi-Fi (the device vibrates when it receives such a signal, thus implementing haptic feedback). Furthermore, movement complete confirmations are sent back to the device over Wi-Fi for use in playback functionality. The computed motor and gripper signals are sent via a wired USB-to-Serial connection to the robotic arm, which then moves accordingly. Record functionality is implemented by saving the accumulated device movements when the user hits the record button on the main screen. Playback transmits each movement snapshot sequentially to the MATLAB server, waiting for the movement complete signal from the server between sends of movement snapshots. “Return Robotic Arm to Home Position” and “Client Is Disconnecting From Server” signals are sent from the device to MATLAB via special values in the gripper section of the regular Wi-Fi packets.

The A.R.M. development team achieved full functionality with three caveats. On the mobile device, movements were successfully recorded and converted into absolute coordinates. Data packets were successfully transmitted over Wi-Fi to a host machine running MATLAB, which in turn was able to move the robotic arm as the user intended. The first two caveats are here, as when the user holds the main button down for an extended period of time, the speed values accumulate error, thus producing erroneous displacement values. This was partially mitigated through various noise reduction algorithms (mentioned above) and the zeroing of speed whenever the user released the main button. However, further work on noise reduction is necessary to improve functionality and usability. Furthermore, the robotic arm does not react in real-time to device movements. While the reaction times are fast, further work (including investigating Bluetooth data transmission and alternatives to our MATLAB implementation) might help to decrease lag. Axis locking, gripper control via a slider UI, haptic feedback, sensor calibration, and the settings activity (with calibration and settings values persistent via internal storage) were all successfully implemented. Arm and Wrist Modes were also functional, along with a “Home” button to return the robotic arm to its home position. Finally, record/playback functionality was successfully implemented, with surprisingly good accuracy. A user is able to record snapshots of the current robotic arm position and then play these positions back sequentially. This is where the third caveat comes in, as playback has accuracy issues if the user records too few snapshots. It is postulated that this is a combination of motor noise and inverse kinematics calculations that produce different robotic arm movements to a specified position with and without intermediate steps. Implementing a feedback controller might alleviate this issue.