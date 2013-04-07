%%  Lab 2 ECE 470 
%   Rene Rail-Ip 996027954
%   Nitharson M 996068008
%  
%
%  Defines a new robot structure with the AX12 parameters
%  determined in the preparation.
%
%  Usage: myrobot = myax12(DH)

function ax12 = myax12(DH)

% Set up DH table

L1 = link([DH(1,2) DH(1,1) DH(1,4) DH(1,3)], 'standard');
L2 = link([DH(2,2) DH(2,1) DH(2,4) DH(2,3)], 'standard');
L3 = link([DH(3,2) DH(3,1) DH(3,4) DH(3,3)], 'standard');
L4 = link([DH(4,2) DH(4,1) DH(4,4) DH(4,3)], 'standard');

ax12 = robot({L1 L2 L3 L4});
