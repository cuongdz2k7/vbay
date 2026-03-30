package com.vbay.network;

import com.vbay.shared.protocol.Request;
import com.vbay.shared.protocol.Respond;
import com.vbay.shared.enums.RequestType;
// Dispatcher --> serperate enusm , RequestType<?> để chuyển sang nơi xử lý data chính
public class Distribution {
    private final //AuthService autheService ;  giao dien
    private final //AuctionService auctionService; 
    private final // PaymentService paymentService;
    
    public Respond<?> distribute(Request<?> request){
        switch (request.getType()) {
            case REGISTER , LOGIN,LOGOUT,FORGOT_PASSWORD ,VERIFY,UPDATE_PROFILE:
                //return authservice.solve(request)
                break;
            case    CLOSE_AUCTION,DECLARE_WINNER,NOTIFY_WINNER: 
                //return auctionservice.solve(request)

            // case . v.v
            default:
                return new Respond<>(request.getRequestId(), false, "Unsupported request", null);
                break;
        }
    }
}
