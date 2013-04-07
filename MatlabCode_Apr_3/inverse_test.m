clear all
close all

%  Declare the DH of the AX12 Robot

DH = [58        pi/2    142     0;
      173       0       0       0;
      23        pi/2    0       0;
      0         0       205     0];
    
%  call the function to create a robot model from the toolkit  
ax12 = myax12(DH);
dh = ax12.dh;
alpha = dh(:,1);
a = dh(:,2);
d = dh(:,4);

%% Commanding the AX-12A robot arm through Matlab

m = manipulator;

% x = 50:200/100:250;
% z = 0:300/100:300;
% 
% [X,Z] = meshgrid(x,z);

% q = getangles(m);
q = [0 0 0 0];
setangles(m,q);
disp('Press a key for Test 1');

pause(1)

o4 = forward_ax12(ax12, q, 4);

z = 400;
y = 0;
x = 0;
config = 'D';

o = [0;0;320];
q = setPos(m, ax12, o);

z = -100; 

 for i =  1:20
    o = [x(i) ; y; z];
    q = inverse_ax12(ax12, o, 3, config);
    q(4) = 0;
    o_new = forward_ax12(ax12, q, 4);
    error(i,:) = o-o_new;
    setangles(m,q);
    pause(0.1);
    
end
    