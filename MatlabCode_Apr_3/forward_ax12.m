%%  Lab 2 ECE 470 
%   Rene Rail-Ip 996027954
%   Nitharson M 996068008
%  
%
%  Function returns H, the homogeneous transformation  matrix H04 = 
%  A1*A2*A3*A4
%
%  Usage: H = forward_ax12(myrobot, q)
%
%  where joint is a 4x1 vector containing 4 joint angles
%  and myrobot is the robot structure genrated by the function myax12

function o = forward_ax12(myrobot, q,joint)

H = eye(4);

%  obtaining DH parameters from myrobot structure
dh = myrobot.dh;
alpha = dh(:,1);
a = dh(:,2);
d = dh(:,4);

%  iteratively compute the homogeneous transformation matrix 
for i = 1:joint
    H = H*[cos(q(i))    -sin(q(i))*cos(alpha(i))    sin(q(i))*sin(alpha(i))     a(i)*cos(q(i));
          sin(q(i))     cos(q(i))*cos(alpha(i))     -cos(q(i))*sin(alpha(i))    a(i)*sin(q(i));
          0             sin(alpha(i))               cos(alpha(i))               d(i);
          0             0                           0                           1];
end

o = H(1:3,4);


 