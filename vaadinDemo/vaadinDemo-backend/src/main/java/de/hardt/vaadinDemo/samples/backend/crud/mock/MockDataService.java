package de.hardt.vaadinDemo.samples.backend.crud.mock;

import java.util.List;

import de.hardt.vaadinDemo.samples.backend.CrudDataService;
import de.hardt.vaadinDemo.samples.backend.crud.data.Category;
import de.hardt.vaadinDemo.samples.backend.crud.data.Product;

/**
 * Mock data model. This implementation has very simplistic locking and does not
 * notify users of modifications.
 */
public class MockDataService extends CrudDataService {
	private static final long serialVersionUID = 2475620906138049479L;

	private static MockDataService INSTANCE;

    private List<Product> products;
    private List<Category> categories;
    private int nextProductId = 0;

    private MockDataService() {
        categories = MockDataGenerator.createCategories();
        products = MockDataGenerator.createProducts(categories);
        nextProductId = products.size() + 1;
    }

    public synchronized static CrudDataService getInstance() {
        if (INSTANCE == null) {
            INSTANCE = new MockDataService();
        }
        return INSTANCE;
    }

    @Override
    public synchronized List<Product> getAllProducts() {
        return products;
    }

    @Override
    public synchronized List<Category> getAllCategories() {
        return categories;
    }

    @Override
    public synchronized void updateProduct(Product p) {
        if (p.getId() < 0) {
            // New product
            p.setId(nextProductId++);
            products.add(p);
            return;
        }
        for (int i = 0; i < products.size(); i++) {
            if (products.get(i).getId() == p.getId()) {
                products.set(i, p);
                return;
            }
        }

        throw new IllegalArgumentException("No product with id " + p.getId()
                + " found");
    }

    @Override
    public synchronized Product getProductById(int productId) {
        for (int i = 0; i < products.size(); i++) {
            if (products.get(i).getId() == productId) {
                return products.get(i);
            }
        }
        return null;
    }

    @Override
    public synchronized void deleteProduct(int productId) {
        Product p = getProductById(productId);
        if (p == null) {
            throw new IllegalArgumentException("Healthcheck with id " + productId
                    + " not found");
        }
        products.remove(p);
    }
}