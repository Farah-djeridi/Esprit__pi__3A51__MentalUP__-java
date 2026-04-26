package Services;

import com.twilio.Twilio;
import com.twilio.rest.api.v2010.account.Message;
import com.twilio.type.PhoneNumber;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

public class SmsService {
    private static String ACCOUNT_SID;
    private static String AUTH_TOKEN;
    private static String TWILIO_NUMBER;

    static {
        Properties props = new Properties();
        try (InputStream is = SmsService.class.getResourceAsStream("/groq.properties")) {
            if (is != null) {
                props.load(is);
                ACCOUNT_SID = props.getProperty("twilio.account_sid");
                AUTH_TOKEN = props.getProperty("twilio.auth_token");
                TWILIO_NUMBER = props.getProperty("twilio.number");
            }
        } catch (IOException e) {
            System.err.println("Erreur de chargement des propriétés Twilio : " + e.getMessage());
        }


        try {
            if (ACCOUNT_SID != null && AUTH_TOKEN != null) {
                Twilio.init(ACCOUNT_SID, AUTH_TOKEN);
            }
        } catch (Exception e) {
            System.err.println("Erreur d'initialisation Twilio : " + e.getMessage());
        }
    }

    public static void sendSms(String to, String text) {
        String formattedTo = formatPhoneNumber(to);
        try {
            Message message = Message.creator(
                    new PhoneNumber(formattedTo),
                    new PhoneNumber(TWILIO_NUMBER),
                    text
            ).create();
            System.out.println("SMS envoyé avec succès ! SID: " + message.getSid());
        } catch (Exception e) {
            System.err.println("Erreur lors de l'envoi du SMS : " + e.getMessage());
        }
    }

    private static String formatPhoneNumber(String phone) {
        if (phone == null || phone.isEmpty()) return phone;
        
       
        String cleaned = phone.replaceAll("[^0-9+]", "");
        
       
        if (cleaned.length() == 8 && cleaned.matches("\\d+")) {
            return "+216" + cleaned;
        }
        
       
        if (!cleaned.startsWith("+")) {
            return "+" + cleaned;
        }
        
        return cleaned;
    }

    public static void notifyRdvConfirmation(String to, String date, String heure) {
        String msg = "Votre rendez-vous sur MentalUp a été confirmé pour le " + date + " à " + heure + ".";
        sendSms(to, msg);
    }

    public static void notifyRdvAnnulation(String to, String date) {
        String msg = "Votre rendez-vous du " + date + " a malheureusement été annulé ou rejeté par le psychologue.";
        sendSms(to, msg);
    }
}
