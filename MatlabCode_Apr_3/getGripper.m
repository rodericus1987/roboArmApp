function gripper = getGripper(m)

    gripper = int32(calllib('dynamixel','dxl_read_word',7,36));
    
end