function setspeed(m,speed)
% Setspeed 
%
% Setspeed changes the speed at which servos move from their current
% position to their setpoint positions. 
%
% setspeed(m,speed)
%
% The speed parameters may vary from 0 to 1023 and is set by default at
% 128. This parameter has been preset to balance responsiveness and safety
% when using the robot.
if nargin < 1
    fprintf('Usage: setspeed(m,speed)\n');
    return;
end


if strcmp(m.type,'sg5')
    if speed<0 || speed >60
        fprintf('Speed not within available range\n');
        fprintf('Set speed between 0 and 60\n');
        return;
    end
    
    m.ramp_rate = speed;
    
elseif strcmp(m.type,'ax12')
    if speed<0 || speed >1023
        fprintf('Speed not within available range\n');
        fprintf('Set speed between 1 and 1023\n');
        return;
    end
    
    for id = 1:7
       calllib('dynamixel','dxl_write_word',id,32,speed);
    end
end
end