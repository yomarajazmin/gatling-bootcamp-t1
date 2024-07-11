package templates;

public class Templates {

    public static String createObjectPayload(String name, String year, String price, String model, String disk){
        return "{\n" +
                "   \"name\": \""+name+"\",\n" +
                "   \"data\": {\n" +
                "      \"year\": "+year+",\n" +
                "      \"price\": "+price+",\n" +
                "      \"CPU model\": \""+model+"\",\n" +
                "      \"Hard disk size\": \""+disk+"\"\n" +
                "   }\n" +
                "}";
    }
}
