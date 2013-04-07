function sendMessage(socket, message)

    % add necessary java libraries java tcp server
    import java.net.*;
    import java.io.*;
    javaaddpath C:\Program Files\MATLAB\R2012a\java\include

    output_stream   = socket.getOutputStream;
    d_output_stream = DataOutputStream(output_stream);

    d_output_stream.writeBytes(char(message));
    d_output_stream.flush;
end