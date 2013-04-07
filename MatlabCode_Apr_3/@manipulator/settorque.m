function settorque(m,state)
% Settorque
%
% Settorque enables or disables motor torque allowing the robot to be moved
% by hand to new positions or to hold preset positions. The set torque 
% function disables torque on all motors including the gripper. 
%
% settorque(m)
% settorque(m,state)
%
% When state is 0 the torque of the servo motors will be disabled to allow
% free motion of the arm. When state is 1 the motor torque will be enabled
% allowing the robot arm to hold an arbitrary postion. If state is not
% specified the function will by default shutdown all motors (state == 0).

if nargin < 2 
    state = 0;  % By default shutdown all motors
end
if nargin < 1
    fprintf('settorque: manipulator is not specified.\n');
    return;
end
for adr = 1:7
    calllib('dynamixel','dxl_write_byte',adr,24,state);
end
end