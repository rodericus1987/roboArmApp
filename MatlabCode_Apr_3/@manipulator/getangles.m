function angles = getangles(m,mode)
% Getangles retreives joint angles.
%
% Getangles initiates communication with the robot arm and retrieves joint
% angles from the arm. The units of the returned angles are variable.
%
%
% angles = getangles(m)
% angles = getangles(m,mode)
%
% mode = 'radians' [default]
% mode = 'degrees' degrees
% mode = 'raw' raw counts [0,1023]

if nargin < 2
    mode = 'radians';
end
if nargin < 1
    fprintf('getangles: manipulator is not specified.\n');
    return;
end
angles = zeros(4,1);
%Read angles
angles(1) = int32(calllib('dynamixel','dxl_read_word',1,36));
angles(2) = int32(calllib('dynamixel','dxl_read_word',2,36));
angles(3) = int32(calllib('dynamixel','dxl_read_word',4,36));
angles(4) = int32(calllib('dynamixel','dxl_read_word',6,36));
%We are no longer including the hand state in the angles function
%angles(5) = int32(calllib('dynamixel','dxl_read_word',7,36));

%For debugging
%fprintf('%d ',angles);
%fprintf('/n');

%Perform scaling
%Joint 1
%angles(1) = angles(1) - 512;
angles(1) = m.C1(1)*(angles(1) - m.C2(1));
%Joint 2
%angles(2) = 1024-angles(2)-int32(105*1024/300);
angles(2) = m.C1(2)*(angles(2) - m.C2(2));
%Joint 3
%angles(3) = angles(3) - 512;
angles(3) = m.C1(3)*(angles(3) - m.C2(3));
%Joint 4
%angles(4) = angles(4) - 512;
angles(4) = m.C1(4)*(angles(4) - m.C2(4));
%Store state of arm in class
m.ang = angles;
%fprintf('%d ',angles);
%fprintf('/n');
%Transform into radians/degrees
if(strcmp(mode,'radians') == 1)
    angles = angles*(300/1024)*(pi/180);
elseif(strcmp(mode,'degrees') == 1)
    angles = angles*300/1024;
end
end