package examschd.service.readers;

import java.util.List;

public interface CsvReader<T> {
    List<T> read(String filePath) throws Exception;
}
