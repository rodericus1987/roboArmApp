function angle = setgripper(m,angle)
% Setgripper changes the state of the gripper.
%
% Setgripper sets the angle of the gripper servo and in doing so can open
% and close the gripper.
%
% setgripper(m,state)
%
% The state variable may be 'open' to move the gripper to the open position
% or 'closed' to close the gripper.
%
% open is an angle of 540, closed is an angle of 720

if angle > 720
    angle = 720;
end
if angle < 550
    angle = 550;
end

calllib('dynamixel','dxl_write_word',7,30,angle);
end