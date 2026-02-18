package app.sandori.stocker.ingest.news.provider;

import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;

@Component
public class ProviderRegistry {

    private final Map<String, NewsProvider> providers;

    public ProviderRegistry(List<NewsProvider> providerList) {
        this.providers = providerList.stream()
                .collect(Collectors.toMap(NewsProvider::id, Function.identity()));
    }

    public Optional<NewsProvider> get(String id) {
        return Optional.ofNullable(providers.get(id));
    }

    public List<NewsProvider> getAll() {
        return List.copyOf(providers.values());
    }

    public List<String> getAllIds() {
        return List.copyOf(providers.keySet());
    }
}
