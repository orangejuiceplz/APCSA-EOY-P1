package com.dl_labs.utilities;

import java.io.IOException;
import java.net.*;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.regex.Pattern;

public class NetworkUtils {
    
    // this was all AI
    // this is just a regex to check if the string is a valid IPv4 address
    // it checks if the string is in the format of 0-255.0-255.0-255.0-255
    private static final Pattern IP_PATTERN = Pattern.compile(
            "^(25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)\\." +
            "(25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)\\." +
            "(25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)\\." +
            "(25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)$");
}
