package com.ringkasanbuku.repository;

import com.google.gson.*;
import com.google.gson.reflect.TypeToken;
import com.ringkasanbuku.model.Summary;

import java.io.IOException;
import java.lang.reflect.Type;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

public class HistoryRepository {
    private static final String PATH = "data/history.json";
    
    private final Gson gson = new GsonBuilder()
            .registerTypeAdapter(LocalDateTime.class, new JsonSerializer<LocalDateTime>() {
                @Override
                public JsonElement serialize(LocalDateTime src, Type typeOfSrc, JsonSerializationContext context) {
                    return new JsonPrimitive(src.format(DateTimeFormatter.ISO_LOCAL_DATE_TIME));
                }
            })
            .registerTypeAdapter(LocalDateTime.class, new JsonDeserializer<LocalDateTime>() {
                @Override
                public LocalDateTime deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
                    return LocalDateTime.parse(json.getAsString(), DateTimeFormatter.ISO_LOCAL_DATE_TIME);
                }
            })
            .setPrettyPrinting()
            .create();

    public HistoryRepository() {
        try {
            Path path = Path.of(PATH);
            if (!Files.exists(path.getParent())) {
                Files.createDirectories(path.getParent());
            }
            if (!Files.exists(path)) {
                Files.writeString(path, "[]");
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void save(Summary summary) {
        List<Summary> all = loadAll();
        all.add(0, summary); // newest first
        writeToFile(all);
    }

    public List<Summary> loadAll() {
        try {
            String content = Files.readString(Path.of(PATH));
            Type listType = new TypeToken<ArrayList<Summary>>(){}.getType();
            List<Summary> summaries = gson.fromJson(content, listType);
            return summaries != null ? summaries : new ArrayList<>();
        } catch (IOException | com.google.gson.JsonSyntaxException e) {
            System.err.println("Gagal membaca history.json (mungkin korup): " + e.getMessage());
            return new ArrayList<>();
        }
    }

    public void delete(String id) {
        List<Summary> all = loadAll();
        all.removeIf(s -> s.getId().equals(id));
        writeToFile(all);
    }

    public Summary findById(String id) {
        return loadAll().stream()
                .filter(s -> s.getId().equals(id))
                .findFirst()
                .orElse(null);
    }

    private void writeToFile(List<Summary> summaries) {
        try {
            Files.writeString(Path.of(PATH), gson.toJson(summaries));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
