function [m error] = manipulator(serialport, type)
% Robot arm control class
%
% This class and related functions provide an interface to control
% the AX-12 robot arm.
% 
% Available functions
% manipulator - create a robot arm object
% getangles - retreive robot arm joint angles
% setangles - set robot arm joint angles
% settorque - enable or disable the actuator to allow free motion of arm
% recordpath - sample position of actuators while manually move arm through space
% playpath - use predetermined set of times and joint positions to set path
% of robot
% setspeed - adjust motion speed for robot
% close - close and cleanup manipulator object
%
% Manipulator allows creation of a robot object. Ensure that the robot arm
% connected to a computer usb port and is powered on before attempting to
% create the object.
%
% m = manipulator(type)
%
% AX12A robot arm: type = 'ax12' (default)
% SG5/6-UT robot arm: type = 'sg5' 

path(path,'C:\Program Files\Robotis\src');
path(path,'C:\Program Files\Robotis\import');

% clear classes;
% clear manipulator;

error = 0;
if nargin < 2
    type = 'ax12';
end
%retain type
m.type = type;
if strcmp(type,'sg5')
    m.ser = serial('COM12');
    m.ser.BaudRate = 2400;
    m.ser.StopBits = 2;
    m.ser.FlowControl = 'NONE';
    m.ser.DataBits = 8;
    fopen(m.ser);

    ver = [0,0,0];
    while (ver ~= '1.0')
        pause(5);
        fwrite(m.ser,['!SCVER?',13]);
        ver = fread(m.ser,3)';
    end
    fprintf('Ready...\n');

    m.ramp_rate = 16;
    define bounds of movement for servos in arm
    m.toprange = [pi,pi-0.4,pi-0.9,pi,pi,pi-0.1];
    m.lowrange = [0.1,0.4,0.1,0,0,1.8];
elseif strcmp(type,'ax12')
    loadlibrary('dynamixel','dynamixel.h');
    %libfunctions('dynamixel'); %prints available functions in library
    m.res = calllib('dynamixel','dxl_initialize',serialport,1);
    if m.res ~= 1
        fprintf('Unable to open dynamixel\n')
        error = 1;
        return;
    end
    %m.toprange = [1024 1024 1024 1024 1024 1024 1024];
    %m.lowrange = [0 0 0 0 0 0 0];
    m.toprange = [1024 800 1024-100 900 900 1024 1024*1.22/pi];
    m.lowrange = [0 100 1024-800 100 100 100 1024*0.697/pi];
    m.speed = 0;
    %Used for calculation of position of two slave servos
    m.pos2 = 0;
    m.pos3 = 0;
    m.pos4 = 0;
    m.pos5 = 0;
    %Used to store last known position of arm
    m.ang = zeros(1,7);
    %Define scaling variables. Scaling is of the form
    %Out = C1*(Raw - C2) where each of these is defined for the 
    %4 joints of the robot arm
    m.C1 = [1 -1 1 1];
    m.C2 = [512 640 200 200];
else
    fprintf('manipulator: invalid type\n');
    return;
end
m = class(m,'manipulator');
if strcmp(type,'ax12')
    setspeed(m,128);
end
end