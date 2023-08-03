package shopping.application;

import java.util.List;
import java.util.stream.Collectors;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import shopping.domain.Product;
import shopping.dto.response.ProductResponse;
import shopping.repository.ProductRepository;

@Service
@Transactional(readOnly = true)
public class ProductService {

    private static final int PRODUCT_PAGE_SIZE = 12;
    public static final int PAGE_START_NUMBER = 1;

    private final ProductRepository productRepository;

    public ProductService(ProductRepository productRepository) {
        this.productRepository = productRepository;
    }

    public List<ProductResponse> findAll() {
        return productRepository.findAll()
                .stream()
                .map(ProductResponse::of)
                .collect(Collectors.toUnmodifiableList());
    }

    public Page<ProductResponse> findAllByPage(Integer pageNumber) {
        int page = pageNumber < PAGE_START_NUMBER
                ? PAGE_START_NUMBER : pageNumber;
        Page<Product> products = productRepository
                .findAll(PageRequest.of(page - PAGE_START_NUMBER, PRODUCT_PAGE_SIZE));
        return products.map(ProductResponse::of);
    }
}
