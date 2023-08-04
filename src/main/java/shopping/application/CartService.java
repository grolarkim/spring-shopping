package shopping.application;

import java.util.List;
import java.util.stream.Collectors;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import shopping.domain.CartItem;
import shopping.domain.Email;
import shopping.domain.Product;
import shopping.domain.User;
import shopping.dto.request.CartItemCreateRequest;
import shopping.dto.request.CartItemUpdateRequest;
import shopping.dto.response.CartItemResponse;
import shopping.exception.CartItemNotFoundException;
import shopping.exception.ProductAlreadyInCartException;
import shopping.exception.ProductNotFoundException;
import shopping.exception.UserNotFoundException;
import shopping.exception.UserNotMatchException;
import shopping.repository.CartItemRepository;
import shopping.repository.ProductRepository;
import shopping.repository.UserRepository;

@Service
@Transactional(readOnly = true)
public class CartService {

    private final CartItemRepository cartItemRepository;
    private final UserRepository userRepository;
    private final ProductRepository productRepository;

    public CartService(CartItemRepository cartItemRepository, UserRepository userRepository,
            ProductRepository productRepository) {
        this.cartItemRepository = cartItemRepository;
        this.userRepository = userRepository;
        this.productRepository = productRepository;
    }

    @Transactional
    public void createCartItem(String email, CartItemCreateRequest cartItemCreateRequest) {
        User user = userRepository.findByEmail(new Email(email))
                .orElseThrow(() -> new UserNotFoundException(email));
        Product product = productRepository.findById(cartItemCreateRequest.getProductId())
                .orElseThrow(() -> new ProductNotFoundException(
                        String.valueOf(cartItemCreateRequest.getProductId())));

        validateProductNotInCart(user, product);

        cartItemRepository.save(new CartItem(user, product));
    }

    private void validateProductNotInCart(User user, Product product) {
        cartItemRepository.findByUserAndProduct(user, product)
                .ifPresent(item -> {
                    throw new ProductAlreadyInCartException();
                });
    }

    public List<CartItemResponse> findAllByEmail(String email) {
        List<CartItem> cartItems = cartItemRepository.findAllByUserEmail(new Email(email));
        return cartItems.stream()
                .map(CartItemResponse::of)
                .collect(Collectors.toUnmodifiableList());
    }

    @Transactional
    public void updateCartItemQuantity(
            String email,
            Long cartItemId,
            CartItemUpdateRequest cartItemUpdateRequest) {

        CartItem cartItem = findCartItem(email, cartItemId);

        CartItem updatedCartItem = cartItem.updateQuantity(cartItemUpdateRequest.getQuantity());
        cartItemRepository.save(updatedCartItem);
    }

    @Transactional
    public void deleteCartItem(String email, Long cartItemId) {
        findCartItem(email, cartItemId);

        cartItemRepository.deleteById(cartItemId);
    }

    private CartItem findCartItem(String email, Long cartItemId) {
        User user = userRepository.findByEmail(new Email(email))
                .orElseThrow(() -> new UserNotFoundException(email));
        CartItem cartItem = cartItemRepository.findById(cartItemId)
                .orElseThrow(() -> new CartItemNotFoundException(String.valueOf(cartItemId)));

        validateUserMatch(user, cartItem);
        return cartItem;
    }

    private void validateUserMatch(User user, CartItem cartItem) {
        if (cartItem.isDifferentUser(user)) {
            throw new UserNotMatchException();
        }
    }
}
