%% The main function for running the A.R.M. Android application

% Handle server being previously open
if exist('connected')
    connected.close
    Server.close
end

close all
clear all

% add necessary java libraries java tcp server
import java.net.*;
import java.io.*;
javaaddpath C:\code_Apr_3\include

% global constant definitions
GRIPOPEN = 550;
GRIPCLOSED = 720;
COMM_RXSUCCESS = 1;
PORT = 4012;  % Port for TCP/IP communication
SERVER_TIMEOUT = 40000;
O_HOME = [300; 0; 300];
ROLL_HOME = pi/2;
CHECKPOINT = 2;
SERIALPORT = 2;

%  Declare the DH of the AX12 Robot
DH = [58        pi/2    142     0;
      173       0       0       0;
      23        pi/2    0       0;
      0         0       205     0];
    
%  call the function to create a robot model from the toolkit  
ax12 = myax12(DH);

% global variables
gripCounts = GRIPOPEN;   % gripper angle in counts
roll = 0;
pitch = 0;
vibrate = 0;
armMode = 1;
moveFlag = 1;
config = 'D';
currentGrip = 0;        % the current gripper in 0-100 range
gripperIn = 0;          
xIn = 0;
yIn = 0;
zIn = 0;
rollIn = 0;
pitchIn = 0;

%% Commanding the AX-12A robot arm through Matlab

[m error] = manipulator(SERIALPORT);

%% Java Server Connection

%% Create Socket
Server = ServerSocket (PORT);

%set timeout
Server.setSoTimeout(SERVER_TIMEOUT);

%% Listen to connection
disp('Waiting for a connection...')

try 
    connected = Server.accept;
catch
    if ~isempty(Server)
        Server.close
    end
    disp('Connection Timed Out');
end

if exist('connected')
    pause(1);
    
  
    commStatus = int32(calllib('dynamixel','dxl_get_result'));
    if commStatus == COMM_RXSUCCESS
        printErrorCode();
    else
        disp('There is a communication error');
        printCommStatus(commStatus);
    end            

    o = O_HOME;
    [q o] = setPos(m, ax12, o, ROLL_HOME);
    q(4) = ROLL_HOME;
    setgripper(m,gripCounts);

    
    
    disp('Connected!');
    %% Begin operation of A.R.M.
    while(1)
        checkConnect = getangles(m);
        if checkConnect(3) == checkConnect(4)
            [m error] = manipulator(SERIALPORT);
        end
        input_stream   = connected.getInputStream;
        d_input_stream = DataInputStream(input_stream);
        bytes_available = input_stream.available;
        vibrate = 0;
        if (bytes_available >= 24) && mod(bytes_available,24) == 0
            data_reader = DataReader(d_input_stream);
            message = data_reader.readBuffer(bytes_available);

            value = double(typecast(message,'single'))
                            
            % check for disconnect command, always at the last value
            if value(bytes_available/4) == 200
                disp('Client Disconnected!');
                break;
            end           
            
            sumValue = sum(value);
            gripperIn = value(6);
            
            % sumValue ~= 0 means it's not an empty packet
            % sumValue could be 0 but gripper request changed from 100 to 0
            if sumValue ~= 0 || gripperIn ~= currentGrip
                vibrate = 0;
                tempSum = 0;
                
                % check all roll and pitch values in the packet to see if
                % they are zero
                for i = 1:(bytes_available/24)
                    tempSum = tempSum + value((i-1)*6+1) + value((i-1)*6+2);
                end
                
                % if all zero, we are in arm mode, otherwise wrist mode
                % do all data packet handling here, accumulating data
                if tempSum == 0
                    armMode = 1;
                    xIn = 0;
                    yIn = 0;
                    zIn = 0;
                    % accumulate the values in the data packet
                    for i = 1:(bytes_available/24)
                        xIn = xIn + 1000*value((i-1)*6+3);
                        yIn = yIn + 1000*value((i-1)*6+4);
                        zIn = zIn + 1000*value((i-1)*6+5);
                    end
                    disp('Arm Mode')
                else
                    armMode = 0;
                    rollIn = 0;
                    pitchIn = 0;
                    % accumulate the values in the data packet
                    for i = 1:(bytes_available/24)
                        rollIn = rollIn + value((i-1)*6+1);
                        pitchIn = pitchIn + value((i-1)*6+2);
                    end
                    disp('Wrist Mode')
                end
                
                % reset tempSum
                tempSum = 0;
                % check all values except gripper value to see if there is
                % a new motion needed. if there is moveFlag = 1.
                for i = 1:(bytes_available/24)
                    tempSum = tempSum + value((i-1)*6+1) + value((i-1)*6+2) + value((i-1)*6+3) + value((i-1)*6+4) + value((i-1)*6+5);
                end
                if tempSum ~= 0
                    moveFlag = 1;
                end
                
                % loop through packet to search for home command
                for i = 1:(bytes_available/24)
                    % check for home command XX
                    if value((i-1)*6+6) == -300
                        o = O_HOME;
                        [q o] = setPos(m,ax12,o, ROLL_HOME);
                        gripCounts = GRIPOPEN;
                        setgripper(m,gripCounts);
                        currentGrip = 0;
                        moveFlag = 1;
                        disp('Home Command Detected!');
                        pause(0.5);
                    end
                end
  
                if armMode
                    % accumulate relative positions                    
                    o = double(o) + [xIn; yIn; zIn];                
                    % check position limits
                    [vibrate o] = checkPosLimits(ax12, vibrate, o);
                    
                    % compute new inverse kinematics
                    q = inverse_ax12(ax12, o, 3, config, q(4));

                    % check angle limits    
                    [vibrate q] = checkAngLimits(vibrate, q);

                    q = setangles(m,q);
                    
                else                    
                    % accumulate incoming values into roll and pitch
                    q(3) = q(3) + pitchIn;
                    q(4) = q(4) + rollIn;
                    
                    o = forward_ax12(ax12, q, 4);
                    [vibrate o] = checkPosLimits(ax12, vibrate, o);
                    
                    % save q1 and q2 temp values so they do not change
                    % since we are in wrist mode (inverse may update)
                    q1temp = q(1);
                    q2temp = q(2);
                    
                    % XX untested condition to use inverse properly
                    if vibrate
                        q = inverse_ax12(ax12, o, 3, config, q(4));
                    end
                    [vibrate q] = checkAngLimits(vibrate, q);
                    q = [q1temp q2temp q(3) q(4)];
                    q = setangles(m,q);
                end

              % vibrate response to send back to client
                if vibrate
                    output_stream   = connected.getOutputStream;
                    d_output_stream = DataOutputStream(output_stream);
                    d_output_stream.writeBytes(char(vibrate));
                    d_output_stream.flush;
                end
            end
            
            % check if gripper within value and not at current gripper pos
            if(gripperIn <= 100 && gripperIn >= 0 && gripperIn ~= currentGrip)
                currentGrip = gripperIn;
                gripCounts = (GRIPCLOSED-GRIPOPEN)*(value(6)/100) + GRIPOPEN;
                gripCounts = setgripper(m, gripCounts);
                moveFlag = 1;
            end
            
            % Playback response: check if motor angles are where we expect
            % ie. the arm has reached the checkpoint
            angles = getangles(m)';
            
            if norm(q - angles) < 0.1 && abs(gripCounts - getGripper(m)) < 20 && moveFlag
                output_stream   = connected.getOutputStream;
                d_output_stream = DataOutputStream(output_stream);
                d_output_stream.writeBytes(char(CHECKPOINT));
                d_output_stream.flush;
                moveFlag = 0;
                disp('Checkpoint Achieved');
            end
        end
    end
    connected.close;
    Server.close;
end
