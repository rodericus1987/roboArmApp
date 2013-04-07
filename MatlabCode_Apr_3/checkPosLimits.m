function [vibrate o] = checkPosLimits(myrobot, vibrate, o)    

    dh = myrobot.dh;
    alpha = dh(:,1);
    a = dh(:,2);
    d = dh(:,4);
    
    if (o(3) < 20) && (abs(o(1)) < 200) && (abs(o(2)) < 200)
        vibrate = 1;
        if o(3) < 20
            o(3) = 20;
            disp('z-position limit at stand');
        end
        if o(1) > 200
            o(1) = 200;
            disp('x-position positive limit at stand');
        elseif o(1) < -200
            o(1) = -200;
            disp('x-position negative limit at stand');
        end
        if o(2) > 200
            o(2) = 200;
            disp('z-position positive limit at stand');
        elseif o(2) < -200
            o(2) = -200;
            disp('z-position negtaive limit at stand');
        end
    end
    
    if o(3) < -50
        vibrate = 1;
        o(3) = -40;
        disp('z-position limit at table');
    end
    
    if o(3) > 520
        o(3) = 520;
    end
    if o(2) > 420
        o(2) = 420;
    elseif o(2) < -420
        o(2) = -420;
    end
    if o(1) > 420
        o(1) = 420;
    elseif o(1) < -420
        o(1) = -420;
    end
        
end

 
 
