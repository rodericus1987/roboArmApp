function [t x] = recordpath(m,time,rate)
% Recordpath
%
% Recordpath disables the robot arm servos and continuously samples the
% servo positions while the user is free to trace a path with the arm.
% Record path does not capture the gripper position. 
%
% [t x] = recordpath(m)
% [t x] = recordpath(m,time)
% [t x] = recordpath(m,time,rate)
%
% Recordpath by default uses a sampling rate of 10 Hz and will continuously
% sample for 10 seconds. The time and rate parameters can be used to adjust
% the sampling frequency and sampling time. 
%
% Recordpath produces a vector t of times which show when each sample was
% recorded and a matrix x of angles which provides the position of the
% robot sampled in time.
if nargin < 3
    rate = 10;
end
if nargin < 2
    time = 10;
end
if nargin < 1
    fprintf('Must provide robot object as input argument\n');
    return
end

settorque(m,0); %Turn off motors before recording path

n = time*rate;
t = zeros(n,1);
x = zeros(n,4);

for i = 1:n
    pause(1/rate);
    t(i) = i/rate;
    x(i,:) = getangles(m)';
end
end