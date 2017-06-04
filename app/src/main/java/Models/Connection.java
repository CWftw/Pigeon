package Models;



public class Connection {
    public static final int TYPE_COMPUTER = 0;
    public static final int TYPE_PHONE = 1;
    public static final int TYPE_TABLET = 2;
    
    private String ip;
    private String name;
    private int type;
    private String city;
    private String country;
    private long timeStamp;
    private String cookie;

    private Connection(String ip) {
        this.ip = ip;
    }

    public int getType() {
        return type;
    }

    public String getCity() {
        return city;
    }

    public String getCountry() {
        return country;
    }

    public String getIp() {
        return ip;
    }

    public String getName() {
        return name;
    }

    public long getTimeStamp() {
        return timeStamp;
    }

    public static class Builder {
        private Connection connection;
        public Builder(String ip) {
            connection = new Connection(ip);
            connection.city = "";
            connection.country = "";
            connection.name = "";
        }

        public Builder setType(int type) {
            connection.type = type;
            return this;
        }

        public Builder setCountry(String country) {
            connection.country = country;
            return this;
        }

        public Builder setCity(String city) {
            connection.city = city;
            return this;
        }

        public Builder setName(String name) {
            connection.name = name;
            return this;
        }

        public Connection build() {
            return this.connection;
        }

        public Builder setCookie(String cookie) {
            connection.cookie = cookie;
            return this;
        }

        public Builder setTimeStamp(long timeStamp) {
            connection.timeStamp = timeStamp;
            return this;
        }
    }
}
