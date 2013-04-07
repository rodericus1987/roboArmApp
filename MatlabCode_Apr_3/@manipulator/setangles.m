function q = setangles(m,angles,mode)
% Setangles sets joint angles.
%
% Setangles initiates communication with the robot arm and sets the target
% position of each servo on the arm. The units of the returned angles are
% variable. This function does NOT open and close the gripper.
%
%
% setangles(m,angles)
% setangles(m,angles,mode)
%
% The angles input must have length(angles) == 4
%
% mode = 'radians' [default]
% mode = 'degrees' degrees
% mode = 'raw' raw counts [0,1023]

if nargin < 2
    fprintf('Usage: setangles(m,angles)\n');
    return;
end
if nargin == 2
    mode = 'radians';
end

if strcmp(m.type,'sg5') && (length(angles) == 6)
    if ~(angles-m.lowrange > 0) || ~(m.toprange-angles > 0) && mode==0
        fprintf('Set point angles not within available range\n');
        fprintf('Use getrange(...) to find available range\n');
        return;
    end

    %iterate through each actuator
    for i = 0:5
        %Limit angles within permissable range
        if angles(i) < m.lowrange(i)
            angles(i) = m.lowrange(i);
            disp('Angle Limit Reached! ID: ');
            disp(i);
        elseif angles(i) > m.toprange(i)
            angles(i) = m.toprange(i);
            disp('Angle Limit Reached! ID: ');
            disp(i);
        end
        %Write to board
        binrange = dec2bin(angles(i+1)/pi*1000+250,16);
        fwrite(m.ser,['!SC',i,m.ramp_rate,bin2dec(binrange(9:16)),bin2dec(binrange(1:8)),13]);
    end
elseif strcmp(m.type,'ax12') && (length(angles) == 4)
    %Convert to 'raw'
    if(strcmp(mode,'radians') == 1)
        angles = angles*(1024/300)*(180/pi);
    elseif(strcmp(mode,'degrees') == 1)
        angles = angles*1024/300;
    end
    %Undo scaling
    %Joint 1
    %angles(1) = angles(1) - 512;
    angles(1) = angles(1)/m.C1(1) + m.C2(1);
    %Joint 2
    %angles(2) = 1024-angles(2)-int32(105*1024/300);
    angles(2) = angles(2)/m.C1(2) + m.C2(2);
    %Joint 3
    %angles(3) = angles(3) - 512;
    angles(3) = angles(3)/m.C1(3) + m.C2(3);
    %Joint 4
    %angles(4) = angles(4) - 512;
    angles(4) = angles(4)/m.C1(4) + m.C2(4);
    %Ensure that all angle are in32
    angles = int32(angles);
     
    %Find values of angles for the two actuators which rotate in opposite
    %directions eg. #3 (paired with master #2) and #5 (paired with master
    %number #4)
    %Modified to removed the hand as part of the set angles function
    %angles = [angles(1) angles(2) 0 angles(3) 0 angles(4) angles(5)];
    angles = [angles(1) angles(2) 0 angles(3) 0 angles(4)];
    
    %Read current positions and calculate value for servo3
%     m.pos2 = int32(calllib('dynamixel','dxl_read_word',2,36));
%     m.pos3 = int32(calllib('dynamixel','dxl_read_word',3,36));

%   modified to not read the motors (RENE)
    angles(3) = 512-(angles(2)-512); 
    
    %Read current positions and calculate value for servo5
%     m.pos4 = int32(calllib('dynamixel','dxl_read_word',4,36));
%     m.pos5 = int32(calllib('dynamixel','dxl_read_word',5,36));
%     angles(5) = m.pos5 - (angles(4) - m.pos4); 

%   modified to not read the motors (RENE)
    angles(5) = 512-(angles(4)-512); 
    
    %Store state of arm in class
    m.ang = angles;

    %for debugging
    %fprintf('%f \n',angles)
    %return
    for id = 1:6
       %perform hard limiting
       if angles(id) < m.lowrange(id)
           angles(id) = m.lowrange(id);
       elseif angles(id) > m.toprange(id)
           angles(id) = m.toprange(id);
       end
       
       calllib('dynamixel','dxl_write_word',id,30,angles(id));
        
    end
    
    q(1) = double((angles(1)-m.C2(1))*m.C1(1));
    q(2) = double((angles(2)-m.C2(2))*m.C1(2));
    q(3) = double((angles(4)-m.C2(3))*m.C1(3));
    q(4) = double((angles(6)-m.C2(4))*m.C1(4));
    
    if(strcmp(mode,'radians') == 1)
        q = q*(300/1024) *(pi/180);
    elseif(strcmp(mode,'degrees') == 1)
        q = q*(300/1024);    
    end
else
    fprintf('setangles: invalid dimension for vector angles\n');
end

    

end