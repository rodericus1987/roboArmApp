function close(m)
% Close closes a manipulator object
%
% Close ensures that communication with a robot arm to properly shutdown
% before the manipulator object is deleted.
%
% close(m)
if strcmp(m.type,'sg5')
    fclose(m.ser);
elseif strcmp(m.type,'ax12')
    calllib('dynamixel','dxl_terminate');
    unloadlibrary('dynamixel');
end

clear classes
end