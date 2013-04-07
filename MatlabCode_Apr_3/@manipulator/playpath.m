function playpath(m,t,x)
% Playpath
%
% Playpath sets the joint positions based upon a predetermined matrix
% specififying the joint angles and a vector specifying the time at which
% each joint angles should be set.
%
% playpath(m,t,x)
%
% The matrix x must be contain 4 columns corresponding to each joint and a
% row count equal to the number of time points in t. Playpath expects all
% angles to be presented in radians.
[r c] = size(x);
if numel(t) ~= r
    fprintf('playpath: time and position vectors are not matched\n');
end
%Pad t with an extra element
t = [0; t];
%Replay path
for i = 1:r
    setangles(m,x(i,:));
    pause(t(i+1)-t(i));
end
end