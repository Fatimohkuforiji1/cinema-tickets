package uk.gov.dwp.uc.pairtest.exception;

public class InvalidPurchaseException extends RuntimeException {
    String message;
    public InvalidPurchaseException(String message){
        super(message);
        this.message = message;
    }


}
