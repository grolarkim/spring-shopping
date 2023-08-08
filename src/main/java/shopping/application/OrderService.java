package shopping.application;

import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort.Direction;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import shopping.domain.Cart;
import shopping.domain.CartItem;
import shopping.domain.Email;
import shopping.domain.Order;
import shopping.domain.User;
import shopping.dto.response.OrderResponse;
import shopping.exception.OrderNotFoundException;
import shopping.exception.UserNotFoundException;
import shopping.exception.UserNotMatchException;
import shopping.repository.CartItemRepository;
import shopping.repository.OrderRepository;
import shopping.repository.UserRepository;

@Service
@Transactional(readOnly = true)
public class OrderService {

    private static final int PAGE_START_NUMBER = 1;
    private static final int MIN_PAGE_SIZE = 6;
    private static final int MAX_PAGE_SIZE = 30;
    private static final String ID_COLUMN = "id";

    private final OrderRepository orderRepository;
    private final CartItemRepository cartItemRepository;
    private final UserRepository userRepository;

    public OrderService(OrderRepository orderRepository, CartItemRepository cartItemRepository,
            UserRepository userRepository) {
        this.orderRepository = orderRepository;
        this.cartItemRepository = cartItemRepository;
        this.userRepository = userRepository;
    }

    @Transactional
    public Long createOrder(String email) {
        User user = userRepository.findByEmail(new Email(email))
                .orElseThrow(() -> new UserNotFoundException(email));
        List<CartItem> cartItems = cartItemRepository.findAllByUserEmail(new Email(email));

        Cart cart = new Cart(cartItems, user);
        Order order = cart.toOrder();
        orderRepository.save(order);
        cartItemRepository.deleteAll(cartItems);
        return order.getId();
    }

    public OrderResponse findOrder(String email, Long orderId) {
        User user = userRepository.findByEmail(new Email(email))
                .orElseThrow(() -> new UserNotFoundException(email));
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new OrderNotFoundException(orderId.toString()));
        validateAccess(user, order);

        return OrderResponse.of(order);
    }

    private static void validateAccess(User user, Order order) {
        if (order.isDifferentUser(user)) {
            throw new UserNotMatchException();
        }
    }

    public Page<OrderResponse> findAllOrder(String email, Integer pageNumber, Integer pageSize) {
        int page = validatePageNumber(pageNumber);
        int size = validatePageSize(pageSize);

        Page<Order> orders = orderRepository.findAllByUserEmail(new Email(email),
                PageRequest.of(page - PAGE_START_NUMBER, size, Direction.DESC, ID_COLUMN));

        return orders.map(OrderResponse::of);
    }


    private int validatePageNumber(Integer pageNumber) {
        return pageNumber < PAGE_START_NUMBER
                ? PAGE_START_NUMBER : pageNumber;
    }

    private int validatePageSize(Integer pageSize) {
        if (pageSize > MAX_PAGE_SIZE) {
            return MAX_PAGE_SIZE;
        }
        if (pageSize < MIN_PAGE_SIZE) {
            return MIN_PAGE_SIZE;
        }
        return pageSize;
    }
}
