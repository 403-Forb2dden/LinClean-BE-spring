package com.linclean.global.log;

import com.linclean.global.web.ApiResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.util.Comparator;
import java.util.List;
import java.util.regex.Pattern;
import java.util.stream.Stream;

/**
 * 서버 로그 파일 조회용 내부 엔드포인트.
 *
 * <p>{@code /internal/**} 경로이므로 {@code InternalApiKeyFilter}가 자동 적용된다.
 * 호출 시 {@code X-Internal-Api-Key} 헤더에 {@code internal.api-key} 값이 필요하다.
 * SSH 없이 IntelliJ HTTP Client / 브라우저에서 로그를 확인하기 위한 용도.
 */
@RestController
@RequestMapping("/internal/logs")
public class LogViewController {

    private static final Pattern DATE_PATTERN = Pattern.compile("\\d{4}-\\d{2}-\\d{2}");
    private static final String ACTIVE_FILE = "app.json";

    @Value("${LOG_DIR:logs}")
    private String logDir;

    /** 사용 가능한 로그 파일 목록(파일명/크기). */
    @GetMapping
    public ResponseEntity<ApiResponse<List<LogFileInfo>>> listLogFiles() throws IOException {
        Path dir = Paths.get(logDir);
        if (!Files.isDirectory(dir)) {
            return ResponseEntity.ok(ApiResponse.of(List.of()));
        }
        try (Stream<Path> files = Files.list(dir)) {
            List<LogFileInfo> result = files
                    .filter(LogViewController::isLogFile)
                    .sorted(Comparator.comparing((Path p) -> p.getFileName().toString()).reversed())
                    .map(LogViewController::toInfo)
                    .toList();
            return ResponseEntity.ok(ApiResponse.of(result));
        }
    }

    /**
     * 특정 날짜의 로그 조회. 오늘 날짜는 롤링 전이므로 활성 파일(app.json)을 가리킨다.
     *
     * @param date  yyyy-MM-dd
     * @param level (선택) 해당 레벨 라인만 필터 (예: ERROR)
     * @param lines (선택) 마지막 N줄만 반환
     */
    @GetMapping("/{date}")
    public ResponseEntity<String> getLogByDate(
            @PathVariable String date,
            @RequestParam(required = false) String level,
            @RequestParam(required = false) Integer lines
    ) throws IOException {
        if (!DATE_PATTERN.matcher(date).matches()) {
            return ResponseEntity.badRequest().body("date 형식은 yyyy-MM-dd 여야 합니다.");
        }

        String fileName = date.equals(LocalDate.now().toString())
                ? ACTIVE_FILE
                : "app." + date + ".json";

        Path dir = Paths.get(logDir).toAbsolutePath().normalize();
        Path file = dir.resolve(fileName).normalize();
        if (!file.startsWith(dir) || !Files.isRegularFile(file)) {
            return ResponseEntity.notFound().build();
        }

        List<String> all = Files.readAllLines(file, StandardCharsets.UTF_8);
        Stream<String> stream = all.stream();
        if (level != null && !level.isBlank()) {
            String needle = "\"level\":\"" + level.toUpperCase() + "\"";
            stream = stream.filter(line -> line.contains(needle));
        }
        List<String> filtered = stream.toList();
        if (lines != null && lines > 0 && filtered.size() > lines) {
            filtered = filtered.subList(filtered.size() - lines, filtered.size());
        }

        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType("application/x-ndjson; charset=UTF-8"))
                .body(String.join("\n", filtered));
    }

    private static boolean isLogFile(Path p) {
        String name = p.getFileName().toString();
        return name.startsWith("app") && name.endsWith(".json");
    }

    private static LogFileInfo toInfo(Path p) {
        long size;
        try {
            size = Files.size(p);
        } catch (IOException e) {
            size = -1;
        }
        return new LogFileInfo(p.getFileName().toString(), size);
    }

    public record LogFileInfo(String fileName, long sizeBytes) {
    }
}
