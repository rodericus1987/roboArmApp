function [q o_new] = setPos(m, ax12, o, roll)

    config = 'D';

    q = inverse_ax12(ax12, o, 3, config, roll);
    o_new = forward_ax12(ax12, q, 4);
    error = o_new - o;
    if error(1) > 1 || error(2) > 1 || error(3) > 1
        disp('Kinematics Error!');
    else
        q = setangles(m,q);
    end
    o_new = forward_ax12(ax12, q, 4);