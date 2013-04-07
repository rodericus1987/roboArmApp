function printErrorCode()
% global ERRBIT_VOLTAGE
% ERRBIT_VOLTAGE     = 1;
% global ERRBIT_ANGLE
% ERRBIT_ANGLE       = 2;
% global ERRBIT_OVERHEAT
% ERRBIT_OVERHEAT    = 4;
% global ERRBIT_RANGE
% ERRBIT_RANGE       = 8;
% global ERRBIT_CHECKSUM
% ERRBIT_CHECKSUM    = 16;
% global ERRBIT_OVERLOAD
% ERRBIT_OVERLOAD    = 32;
% global ERRBIT_INSTRUCTION
% ERRBIT_INSTRUCTION = 64;
 if int32(calllib('dynamixel','dxl_get_rxpacket_error', ERRBIT_VOLTAGE))==1
     disp('Input Voltage Error!');
 elseif int32(calllib('dynamixel','dxl_get_rxpacket_error',ERRBIT_ANGLE))==1
     disp('Angle limit error!');
 elseif int32(calllib('dynamixel','dxl_get_rxpacket_error',ERRBIT_OVERHEAT))==1
     disp('Overheat error!');
 elseif int32(calllib('dynamixel','dxl_get_rxpacket_error',ERRBIT_RANGE))==1
     disp('Out of range error!');
 elseif int32(calllib('dynamixel','dxl_get_rxpacket_error',ERRBIT_CHECKSUM))==1
     disp('Checksum error!');
 elseif int32(calllib('dynamixel','dxl_get_rxpacket_error',ERRBIT_OVERLOAD))==1
     disp('Overload error!');
 elseif int32(calllib('dynamixel','dxl_get_rxpacket_error',ERRBIT_INSTRUCTION))==1
     disp('Instruction code error!');
 end
 
end