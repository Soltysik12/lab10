import org.json.JSONObject;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.URL;
import java.nio.charset.Charset;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

    public class ChatBot {
        private final String apiId = "544c8b90d6d83a4a0ec0c7f02801c661";
        private final String townId = "3094802";

        public ChatBot(){

        }

        public String getAnswer(String question){
            switch(question){
                case "time":
                    return getHour();
                case "day":
                    return getDay();
                case "weather":
                    return getWeather();
                default:
                    return "Possible questions:"
                            + "\ntime, \nday or \nweather";
            }
        }

        private String getWeather() {
            String url="http://api.openweathermap.org/data/2.5/weather?id=" + townId + "&APPID=" + apiId;
            StringBuilder weather = new StringBuilder();
            try(BufferedReader br = new BufferedReader(
                    new InputStreamReader(
                            new URL(url).openStream(), Charset.forName("UTF-8")));){
                for(String line ; ( line = br.readLine() ) != null ; ){
                    weather.append(line);
                }

                JSONObject jsonWeather = new JSONObject(weather.toString());

                String main = jsonWeather.getJSONArray("weather").getJSONObject(0).getString("main");
                String temp = jsonWeather.getJSONObject("main").getDouble("temp") + "";

                double dTemp=Double.parseDouble(temp);
                dTemp-=273f;

                return "Weather in Cracow: " + main+" temperature: " + Double.toString(Math.round(dTemp*100)/100);
            }
            catch(Exception ex){

            }
            return "Nie udalo sie pobrac informacji o pogodzie.";
        }

        private String getHour() {
            return new SimpleDateFormat("HH:mm:ss").format(new Date());
        }

        private String getDay(){
            Calendar calendar = Calendar.getInstance();
            Date date = calendar.getTime();
            // 3 letter name form of the day
            String day= new SimpleDateFormat("EE", Locale.ENGLISH).format(date.getTime());
            switch(day){
                case("Mon"):
                    return "Monday";
                case("Tue"):
                    return "Tuesday";
                case("Wed"):
                    return "Wednesday";
                case("Thu"):
                    return "Thursday";
                case("Fri"):
                    return "Friday";
                case("Sat"):
                    return "Saturday";
                case("Sun"):
                    return "Sunday";
                default:
                    return day;
            }
        }
    }

