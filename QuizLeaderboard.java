import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.*;
import java.util.regex.*;


public class QuizLeaderboard {

    
    private static final String REG_NO = "RA2311003020813";

    private static final String BASE_URL  = "https://devapigw.vidalhealthtpa.com/srm-quiz-task";
    private static final int TOTAL_POLLS  = 10;
    private static final int DELAY_MS     = 5000;

    public static void main(String[] args) throws Exception {

        HttpClient client = HttpClient.newHttpClient();

       
        Set<String>     seenEvents = new HashSet<>();
        
        Map<String, Integer> scores = new LinkedHashMap<>();

        System.out.println("========================================");
        System.out.println("   Quiz Leaderboard System Starting     ");
        System.out.println("========================================\n");

        
        for (int poll = 0; poll < TOTAL_POLLS; poll++) {
            System.out.printf("[Poll %d/%d] Calling API with poll=%d ...%n",
                    poll + 1, TOTAL_POLLS, poll);

            String url = BASE_URL + "/quiz/messages?regNo=" + REG_NO + "&poll=" + poll;

            HttpRequest req = HttpRequest.newBuilder()
                    .uri(URI.create(url))
                    .GET()
                    .build();

HttpResponse<String> res = null;

for (int attempt = 1; attempt <= 5; attempt++) {
    try {
        res = client.send(req, HttpResponse.BodyHandlers.ofString());

        if (res.statusCode() == 200) {
            break; 
        } else {
            System.out.println("  ⚠ HTTP " + res.statusCode() + " (Attempt " + attempt + ")");
        }

    } catch (Exception e) {
        System.out.println("  ⚠ Error: " + e.getMessage());
    }

    if (attempt < 5) {
        System.out.println("  Retrying in 5 seconds...");
        Thread.sleep(5000);
    }
}
            if (res.statusCode() != 200) {
                System.out.println("  ⚠  HTTP " + res.statusCode() + " — " + res.body());
            } else {
              
                List<Event> events = parseEvents(res.body());
                int fresh = 0, dupes = 0;

                for (Event e : events) {
                    String key = e.roundId + "|" + e.participant;

                    if (seenEvents.contains(key)) {
                      
                        dupes++;
                    } else {
                        seenEvents.add(key);
                        scores.merge(e.participant, e.score, Integer::sum);
                        fresh++;
                    }
                }
                System.out.printf("  ✓  New events: %d | Duplicates skipped: %d%n", fresh, dupes);
            }

       
            if (poll < TOTAL_POLLS - 1) {
                System.out.println("  Waiting 5 seconds...\n");
                Thread.sleep(DELAY_MS);
            }
        }

      
        List<Map.Entry<String, Integer>> leaderboard = new ArrayList<>(scores.entrySet());
        leaderboard.sort((a, b) -> b.getValue() - a.getValue());

        System.out.println("\n========================================");
        System.out.println("             LEADERBOARD                ");
        System.out.println("========================================");
        int grandTotal = 0;
        for (Map.Entry<String, Integer> entry : leaderboard) {
            System.out.printf("  %-25s %d%n", entry.getKey(), entry.getValue());
            grandTotal += entry.getValue();
        }
        System.out.println("----------------------------------------");
        System.out.println("  Combined Total Score : " + grandTotal);
        System.out.println("========================================\n");

      
        StringBuilder leaderboardJson = new StringBuilder("[");
        for (int i = 0; i < leaderboard.size(); i++) {
            Map.Entry<String, Integer> e = leaderboard.get(i);
            leaderboardJson.append("{\"participant\":\"")
                           .append(e.getKey())
                           .append("\",\"totalScore\":")
                           .append(e.getValue())
                           .append("}");
            if (i < leaderboard.size() - 1) leaderboardJson.append(",");
        }
        leaderboardJson.append("]");

        String body = "{\"regNo\":\"" + REG_NO + "\",\"leaderboard\":" + leaderboardJson + "}";

        System.out.println("Submitting payload:");
        System.out.println(body + "\n");

    
        HttpRequest submitReq = HttpRequest.newBuilder()
                .uri(URI.create(BASE_URL + "/quiz/submit"))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(body))
                .build();

        HttpResponse<String> submitRes = client.send(submitReq, HttpResponse.BodyHandlers.ofString());

        System.out.println("Submit Response (HTTP " + submitRes.statusCode() + "):");
        System.out.println(submitRes.body());

      
        String responseBody = submitRes.body();
        boolean isCorrect = responseBody.contains("\"isCorrect\":true");
        System.out.println("\n" + (isCorrect
                ? "🎉 SUCCESS! Leaderboard accepted."
                : "❌ Submission incorrect — check your regNo and logic."));
    }

    
    private static List<Event> parseEvents(String json) {
        List<Event> events = new ArrayList<>();

      
        int eventsStart = json.indexOf("\"events\"");
        if (eventsStart == -1) return events;

        int arrayStart = json.indexOf('[', eventsStart);
        int arrayEnd   = json.indexOf(']', arrayStart);
        if (arrayStart == -1 || arrayEnd == -1) return events;

        String eventsArray = json.substring(arrayStart + 1, arrayEnd);

       
        Pattern objPattern   = Pattern.compile("\\{[^}]+\\}");
        Pattern roundPattern = Pattern.compile("\"roundId\"\\s*:\\s*\"([^\"]+)\"");
        Pattern partPattern  = Pattern.compile("\"participant\"\\s*:\\s*\"([^\"]+)\"");
        Pattern scorePattern = Pattern.compile("\"score\"\\s*:\\s*(\\d+)");

        Matcher objMatcher = objPattern.matcher(eventsArray);
        while (objMatcher.find()) {
            String obj = objMatcher.group();

            Matcher rm = roundPattern.matcher(obj);
            Matcher pm = partPattern.matcher(obj);
            Matcher sm = scorePattern.matcher(obj);

            if (rm.find() && pm.find() && sm.find()) {
                events.add(new Event(rm.group(1), pm.group(1), Integer.parseInt(sm.group(1))));
            }
        }
        return events;
    }

    
    static class Event {
        String roundId, participant;
        int score;
        Event(String roundId, String participant, int score) {
            this.roundId     = roundId;
            this.participant = participant;
            this.score       = score;
        }
    }
}
