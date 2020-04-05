package mops.gruppen2.service;

import com.fasterxml.jackson.databind.ObjectReader;
import com.fasterxml.jackson.dataformat.csv.CsvMapper;
import com.fasterxml.jackson.dataformat.csv.CsvSchema;
import mops.gruppen2.domain.User;
import mops.gruppen2.domain.exception.EventException;
import mops.gruppen2.domain.exception.WrongFileException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
public final class CsvService {

    private static final Logger LOG = LoggerFactory.getLogger(CsvService.class);

    private CsvService() {}

    static List<User> read(InputStream stream) throws IOException {
        CsvMapper mapper = new CsvMapper();

        CsvSchema schema = mapper.schemaFor(User.class).withHeader().withColumnReordering(true);
        ObjectReader reader = mapper.readerFor(User.class).with(schema);

        return reader.<User>readValues(stream).readAll();
    }

    //TODO: CsvService
    static List<User> readCsvFile(MultipartFile file) throws EventException {
        if (file == null) {
            return new ArrayList<>();
        }
        if (!file.isEmpty()) {
            try {
                List<User> userList = read(file.getInputStream());
                return userList.stream().distinct().collect(Collectors.toList()); //filters duplicates from list
            } catch (IOException ex) {
                LOG.warn("File konnte nicht gelesen werden");
                throw new WrongFileException(file.getOriginalFilename());
            }
        }
        return new ArrayList<>();
    }
}
