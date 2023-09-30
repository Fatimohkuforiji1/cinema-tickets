package uk.gov.dwp.uc.pairtest;

import thirdparty.paymentgateway.TicketPaymentService;
import thirdparty.paymentgateway.TicketPaymentServiceImpl;
import thirdparty.seatbooking.SeatReservationServiceImpl;
import uk.gov.dwp.uc.pairtest.domain.TicketTypeRequest;
import uk.gov.dwp.uc.pairtest.exception.InvalidPurchaseException;

import java.math.BigDecimal;
import java.util.*;
import java.util.stream.Collectors;

public class TicketServiceImpl extends TicketPaymentServiceImpl implements TicketService {
    /**
     * Should only have private methods other than the one below.
     */


    final Map<TicketTypeRequest.Type, BigDecimal> ticketPrice = new HashMap<>();
    final SeatReservationServiceImpl seatReservationService = new SeatReservationServiceImpl();



    @Override
    public void purchaseTickets(Long accountId, TicketTypeRequest... ticketTypeRequests) throws InvalidPurchaseException {
        validateNoOfTickets(ticketTypeRequests);
        validateTicketPurchase(ticketTypeRequests);
        makePayment(accountId,calculateAmountOfTicket(ticketTypeRequests));
        seatReservationService.reserveSeat(accountId, calculateNoOfSeatsToBeAllocated(ticketTypeRequests));
    }

    private void validateNoOfTickets(TicketTypeRequest... ticketTypeRequests) throws InvalidPurchaseException{
        int ticketNo = Arrays.stream(ticketTypeRequests).mapToInt(TicketTypeRequest::getNoOfTickets).sum();
        if(ticketNo > 20){
            throw new InvalidPurchaseException("Number to tickets exceeds maximum no of tickets allowed which is twenty (20)");
        }
    }

    private void validateTicketPurchase(TicketTypeRequest... typeRequests) throws InvalidPurchaseException{
       Arrays.stream(typeRequests).filter(x -> x.getTicketType().equals(TicketTypeRequest.Type.ADULT)).findFirst().orElseThrow( () -> new InvalidPurchaseException("You can not purchase a ticket without purchasing an Adult ticket"));
    }

    private int calculateAmountOfTicket(TicketTypeRequest... typeRequests) {
        int ticketAmount= 0;
        for(TicketTypeRequest ticketTypeRequest: typeRequests){
            int amount = getTicketPrice(ticketTypeRequest.getTicketType()) * ticketTypeRequest.getNoOfTickets();
            ticketAmount += amount;
        }
        return ticketAmount;
    }

    private int calculateNoOfSeatsToBeAllocated(TicketTypeRequest... ticketTypeRequests){
        int noOfSeatsAllocated = 0;
        for(TicketTypeRequest ticketTypeRequest : ticketTypeRequests){
            if(!ticketTypeRequest.getTicketType().equals(TicketTypeRequest.Type.INFANT))
                noOfSeatsAllocated += ticketTypeRequest.getNoOfTickets();
        }
        return noOfSeatsAllocated;
    }

    private int getTicketPrice(TicketTypeRequest.Type ticketType){
        int price;
        switch(ticketType) {
            case ADULT:
                price = 20;
                break;
            case CHILD:
                price = 10;
                break;
            case INFANT:
                price = 0;
                break;
            default:
                throw new IllegalStateException("Unexpected value: " + ticketType);
        }
        return price;
    }

}
