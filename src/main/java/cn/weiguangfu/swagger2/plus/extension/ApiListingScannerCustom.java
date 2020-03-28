package cn.weiguangfu.swagger2.plus.extension;

import cn.weiguangfu.swagger2.plus.plus.DefaultSwagger2Push;
import cn.weiguangfu.swagger2.plus.plus.Swagger2Push;
import com.google.common.base.Joiner;
import com.google.common.base.Predicate;
import com.google.common.base.Splitter;
import com.google.common.collect.LinkedListMultimap;
import com.google.common.collect.Multimap;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.Primary;
import springfox.documentation.PathProvider;
import springfox.documentation.builders.ApiListingBuilder;
import springfox.documentation.schema.Model;
import springfox.documentation.service.*;
import springfox.documentation.spi.service.contexts.ApiListingContext;
import springfox.documentation.spi.service.contexts.DocumentationContext;
import springfox.documentation.spi.service.contexts.RequestMappingContext;
import springfox.documentation.spring.web.paths.PathMappingAdjuster;
import springfox.documentation.spring.web.plugins.DocumentationPluginsManager;
import springfox.documentation.spring.web.scanners.ApiDescriptionReader;
import springfox.documentation.spring.web.scanners.ApiListingScanner;
import springfox.documentation.spring.web.scanners.ApiListingScanningContext;
import springfox.documentation.spring.web.scanners.ApiModelReader;

import java.util.*;

import static com.google.common.collect.FluentIterable.from;
import static com.google.common.collect.Lists.newArrayList;
import static com.google.common.collect.Sets.newHashSet;
import static springfox.documentation.spi.service.contexts.Orderings.methodComparator;
import static springfox.documentation.spi.service.contexts.Orderings.resourceGroupComparator;

@Primary
@Import(DefaultSwagger2Push.class)
//@ComponentScan(basePackages = {"cn.weiguangfu.swagger2.plus.plus"})
@SuppressWarnings("all")
public class ApiListingScannerCustom extends ApiListingScanner {

    @Autowired
    private Swagger2Push swagger2Push;

    private final ApiDescriptionReader apiDescriptionReader;
    private final ApiModelReader apiModelReader;
    private final DocumentationPluginsManager pluginsManager;

    @Autowired
    public ApiListingScannerCustom(ApiDescriptionReader apiDescriptionReader,
                                   ApiModelReader apiModelReader,
                                   DocumentationPluginsManager pluginsManager) {
        super(apiDescriptionReader, apiModelReader, pluginsManager);
        this.apiDescriptionReader = apiDescriptionReader;
        this.apiModelReader = apiModelReader;
        this.pluginsManager = pluginsManager;
    }

    public Multimap<String, ApiListing> scan(ApiListingScanningContext context) {
        Multimap<String, ApiListing> apiListingMap = LinkedListMultimap.create();
        int position = 0;

        Map<ResourceGroup, List<RequestMappingContext>> requestMappingsByResourceGroup
                = context.getRequestMappingsByResourceGroup();
        List<SecurityReference> securityReferences = newArrayList();
        for (ResourceGroup resourceGroup : sortedByName(requestMappingsByResourceGroup.keySet())) {

            DocumentationContext documentationContext = context.getDocumentationContext();
            Set<String> produces = new LinkedHashSet<String>(documentationContext.getProduces());
            Set<String> consumes = new LinkedHashSet<String>(documentationContext.getConsumes());
            String host = documentationContext.getHost();
            Set<String> protocols = new LinkedHashSet<String>(documentationContext.getProtocols());
            Set<ApiDescription> apiDescriptions = newHashSet();

            Map<String, Model> models = new LinkedHashMap<String, Model>();
            for (RequestMappingContext each : sortedByMethods(requestMappingsByResourceGroup.get(resourceGroup))) {
                Map<String, Model> modelMap = apiModelReader.read(each.withKnownModels(models));
                models.putAll(modelMap);
                List<ApiDescription> apiDescriptionList = apiDescriptionReader.read(each);
                apiDescriptions.addAll(
                        swagger2Push.getNewApiDescriptionList(resourceGroup, each, models, apiDescriptionList));
            }

            apiDescriptions.addAll(from(pluginsManager.additionalListings(context))
                    .filter(onlySelectedApis(documentationContext))
                    .toList());

            List<ApiDescription> sortedApis = newArrayList(apiDescriptions);
            Collections.sort(sortedApis, documentationContext.getApiDescriptionOrdering());

            String resourcePath = longestCommonPath(sortedApis);

            PathProvider pathProvider = documentationContext.getPathProvider();
            String basePath = pathProvider.getApplicationBasePath();
            PathAdjuster adjuster = new PathMappingAdjuster(documentationContext);
            ApiListingBuilder apiListingBuilder = new ApiListingBuilder(context.apiDescriptionOrdering())
                    .apiVersion(documentationContext.getApiInfo().getVersion())
                    .basePath(adjuster.adjustedPath(basePath))
                    .resourcePath(resourcePath)
                    .produces(produces)
                    .consumes(consumes)
                    .host(host)
                    .protocols(protocols)
                    .securityReferences(securityReferences)
                    .apis(sortedApis)
                    .models(models)
                    .position(position++)
                    .availableTags(documentationContext.getTags());

            ApiListingContext apiListingContext = new ApiListingContext(
                    context.getDocumentationType(),
                    resourceGroup,
                    apiListingBuilder);
            apiListingMap.put(resourceGroup.getGroupName(), pluginsManager.apiListing(apiListingContext));
        }
        return apiListingMap;
    }

    private Predicate<ApiDescription> onlySelectedApis(final DocumentationContext context) {
        return new Predicate<ApiDescription>() {
            @Override
            public boolean apply(ApiDescription input) {
                return context.getApiSelector().getPathSelector().apply(input.getPath());
            }
        };
    }

    private Iterable<ResourceGroup> sortedByName(Set<ResourceGroup> resourceGroups) {
        return from(resourceGroups).toSortedList(resourceGroupComparator());
    }

    private Iterable<RequestMappingContext> sortedByMethods(List<RequestMappingContext> contexts) {
        return from(contexts).toSortedList(methodComparator());
    }

    static String longestCommonPath(List<ApiDescription> apiDescriptions) {
        List<String> commons = newArrayList();
        if (null == apiDescriptions || apiDescriptions.isEmpty()) {
            return null;
        }
        List<String> firstWords = urlParts(apiDescriptions.get(0));

        for (int position = 0; position < firstWords.size(); position++) {
            String word = firstWords.get(position);
            boolean allContain = true;
            for (int i = 1; i < apiDescriptions.size(); i++) {
                List<String> words = urlParts(apiDescriptions.get(i));
                if (words.size() < position + 1 || !words.get(position).equals(word)) {
                    allContain = false;
                    break;
                }
            }
            if (allContain) {
                commons.add(word);
            }
        }
        Joiner joiner = Joiner.on("/").skipNulls();
        return "/" + joiner.join(commons);
    }

    static List<String> urlParts(ApiDescription apiDescription) {
        return Splitter.on('/')
                .omitEmptyStrings()
                .trimResults()
                .splitToList(apiDescription.getPath());
    }

}