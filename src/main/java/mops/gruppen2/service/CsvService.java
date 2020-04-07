package mops.gruppen2.service;

import com.fasterxml.jackson.databind.ObjectReader;
import com.fasterxml.jackson.dataformat.csv.CsvMapper;
import com.fasterxml.jackson.dataformat.csv.CsvSchema;
import lombok.extern.log4j.Log4j2;
import mops.gruppen2.domain.User;
import mops.gruppen2.domain.exception.EventException;
import mops.gruppen2.domain.exception.WrongFileException;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Log4j2
public final class CsvService {

    private CsvService() {}

    public static List<User> readCsvFile(MultipartFile file) throws EventException {
        if (file == null) {
            return Collections.emptyList();
        }
        if (!file.isEmpty()) {
            try {
                List<User> userList = read(file.getInputStream());
                return userList.stream()
                               .distinct()
                               .collect(Collectors.toList()); //filter duplicates from list
            } catch (IOException ex) {
                log.error("File konnte nicht gelesen werden:\n{}", ex.getMessage());
                throw new WrongFileException(file.getOriginalFilename());
            }
        }
        return Collections.emptyList();
    }

    private static List<User> read(InputStream stream) throws IOException {
        CsvMapper mapper = new CsvMapper();

        CsvSchema schema = mapper.schemaFor(User.class).withHeader().withColumnReordering(true);
        ObjectReader reader = mapper.readerFor(User.class).with(schema);

        return reader.<User>readValues(stream).readAll();
    }
}
