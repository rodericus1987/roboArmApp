function [vibrate q] = checkAngLimits(vibrate, q) 
    % hard limiting

%     if q(2) < -pi/3
%         q(2) = -0.9*pi/3;
%         vibrate = 1;
%         disp('Theta 2 lower limit reached');
%     elseif q(2) > 5*pi/6
%         q(2) = 0.9*5*pi/6;
%         vibrate = 1;
%         disp('Theta 2 upper limit reached');
%     end

    if q(3) < -pi/6
        q(3) = -0.9*pi/6;
        vibrate = 1;
        disp('Theta 3 lower limit reached');
    elseif q(3) > pi
        q(3) = 0.9*pi;
        vibrate = 1;
        disp('Theta 3 upper limit reached');
    end

    if q(4) < 0
        q(4) = 0.1;
        vibrate = 1;
        disp('Theta 4 lower limit reached');
    elseif q(4) > pi
        q(4) = 0.9*pi;
        vibrate = 1;
        disp('Theta 4 upper limit reached');
    end
end