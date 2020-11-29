package lab2.cache.classes;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

class ConnectionHandler  {
    public static String processCommand(Cache cache,String clientString) {
        List<String> args = new ArrayList<>(Arrays.asList(clientString.split("< >")));
        String command = args.remove(0).toUpperCase();
        Object response = null;
        try {
            switch (command) {
                case "SET":
                    cache.set(args.get(0), args.get(1));
                    break;
                case "GET":
                    response = cache.get(args.get(0));
                    break;
                case "SETNX":
                    cache.setnx(args.get(0), args.get(1));
                    break;
                case "MGET":
                    response = cache.mget(args);
                    break;
                case "DEL":
                    response = cache.del(args);
                    break;
                case "INCR":
                    response = cache.incr(args.get(0));
                    break;
                case "LLEN":
                    response = cache.llen(args.get(0));
                    break;
                case "LREM":
                    response = cache.lrem(args.get(0), Integer.parseInt(args.get(1)), args.get(2));
                    break;
                case "LPUSH":
                    response = cache.lpush(args.get(0), args.get(1));
                    break;
                case "RPOPLPUSH":
                    response = cache.rpoplpush(args.get(0), args.get(1));
                    break;
                case "LRANGE":
                    response = cache.lrange(args.get(0), Integer.parseInt(args.get(1)), Integer.parseInt(args.get(2)));
                    break;
                default:
                    throw new Exception("Unknown command" + command);
            }
        } catch ( Exception e) {
            response = e.getMessage();
        }

        String output;

        if (response == null)
            output = "null";
        else if (response instanceof Exception) {
            output = "(error) " + ((Exception) response).getMessage();
        } else if (response instanceof Integer) {
            output = "(integer) " + (Integer) response;
        } else if (response instanceof List) {
            if (((List<Object>) response).isEmpty()) {
                output = "(empty array)";
            } else {
                String joinedString = ((List<Object>) response).stream()
                        .map(object -> Objects.toString(object, null))
                        .collect(Collectors.joining(","));
                output = "(array) " + joinedString;
            }
        } else
            output = (String) response;

        return output;
    }
}