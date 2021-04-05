
package com.app.ekottel.RetroModels;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.util.List;

public class CreatePaymentIntentRes {

    @SerializedName("id")
    @Expose
    private String id;
    @SerializedName("object")
    @Expose
    private String object;
    @SerializedName("allowed_source_types")
    @Expose
    private List<String> allowedSourceTypes = null;
    @SerializedName("amount")
    @Expose
    private Integer amount;
    @SerializedName("amount_capturable")
    @Expose
    private Integer amountCapturable;
    @SerializedName("amount_received")
    @Expose
    private Integer amountReceived;
    @SerializedName("application")
    @Expose
    private Object application;
    @SerializedName("application_fee_amount")
    @Expose
    private Object applicationFeeAmount;
    @SerializedName("canceled_at")
    @Expose
    private Object canceledAt;
    @SerializedName("cancellation_reason")
    @Expose
    private Object cancellationReason;
    @SerializedName("capture_method")
    @Expose
    private String captureMethod;
    @SerializedName("charges")
    @Expose
    private Charges charges;
    @SerializedName("client_secret")
    @Expose
    private String clientSecret;
    @SerializedName("confirmation_method")
    @Expose
    private String confirmationMethod;
    @SerializedName("created")
    @Expose
    private Integer created;
    @SerializedName("currency")
    @Expose
    private String currency;
    @SerializedName("customer")
    @Expose
    private Object customer;
    @SerializedName("description")
    @Expose
    private Object description;
    @SerializedName("invoice")
    @Expose
    private Object invoice;
    @SerializedName("last_payment_error")
    @Expose
    private Object lastPaymentError;
    @SerializedName("livemode")
    @Expose
    private Boolean livemode;
    @SerializedName("metadata")
    @Expose
    private Metadata metadata;
    @SerializedName("next_action")
    @Expose
    private Object nextAction;
    @SerializedName("next_source_action")
    @Expose
    private Object nextSourceAction;
    @SerializedName("on_behalf_of")
    @Expose
    private Object onBehalfOf;
    @SerializedName("payment_method")
    @Expose
    private Object paymentMethod;
    @SerializedName("payment_method_options")
    @Expose
    private PaymentMethodOptions paymentMethodOptions;
    @SerializedName("payment_method_types")
    @Expose
    private List<String> paymentMethodTypes = null;
    @SerializedName("receipt_email")
    @Expose
    private Object receiptEmail;
    @SerializedName("review")
    @Expose
    private Object review;
    @SerializedName("setup_future_usage")
    @Expose
    private Object setupFutureUsage;
    @SerializedName("shipping")
    @Expose
    private Object shipping;
    @SerializedName("source")
    @Expose
    private Object source;
    @SerializedName("statement_descriptor")
    @Expose
    private Object statementDescriptor;
    @SerializedName("statement_descriptor_suffix")
    @Expose
    private Object statementDescriptorSuffix;
    @SerializedName("status")
    @Expose
    private String status;
    @SerializedName("transfer_data")
    @Expose
    private Object transferData;
    @SerializedName("transfer_group")
    @Expose
    private Object transferGroup;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getObject() {
        return object;
    }

    public void setObject(String object) {
        this.object = object;
    }

    public List<String> getAllowedSourceTypes() {
        return allowedSourceTypes;
    }

    public void setAllowedSourceTypes(List<String> allowedSourceTypes) {
        this.allowedSourceTypes = allowedSourceTypes;
    }

    public Integer getAmount() {
        return amount;
    }

    public void setAmount(Integer amount) {
        this.amount = amount;
    }

    public Integer getAmountCapturable() {
        return amountCapturable;
    }

    public void setAmountCapturable(Integer amountCapturable) {
        this.amountCapturable = amountCapturable;
    }

    public Integer getAmountReceived() {
        return amountReceived;
    }

    public void setAmountReceived(Integer amountReceived) {
        this.amountReceived = amountReceived;
    }

    public Object getApplication() {
        return application;
    }

    public void setApplication(Object application) {
        this.application = application;
    }

    public Object getApplicationFeeAmount() {
        return applicationFeeAmount;
    }

    public void setApplicationFeeAmount(Object applicationFeeAmount) {
        this.applicationFeeAmount = applicationFeeAmount;
    }

    public Object getCanceledAt() {
        return canceledAt;
    }

    public void setCanceledAt(Object canceledAt) {
        this.canceledAt = canceledAt;
    }

    public Object getCancellationReason() {
        return cancellationReason;
    }

    public void setCancellationReason(Object cancellationReason) {
        this.cancellationReason = cancellationReason;
    }

    public String getCaptureMethod() {
        return captureMethod;
    }

    public void setCaptureMethod(String captureMethod) {
        this.captureMethod = captureMethod;
    }

    public Charges getCharges() {
        return charges;
    }

    public void setCharges(Charges charges) {
        this.charges = charges;
    }

    public String getClientSecret() {
        return clientSecret;
    }

    public void setClientSecret(String clientSecret) {
        this.clientSecret = clientSecret;
    }

    public String getConfirmationMethod() {
        return confirmationMethod;
    }

    public void setConfirmationMethod(String confirmationMethod) {
        this.confirmationMethod = confirmationMethod;
    }

    public Integer getCreated() {
        return created;
    }

    public void setCreated(Integer created) {
        this.created = created;
    }

    public String getCurrency() {
        return currency;
    }

    public void setCurrency(String currency) {
        this.currency = currency;
    }

    public Object getCustomer() {
        return customer;
    }

    public void setCustomer(Object customer) {
        this.customer = customer;
    }

    public Object getDescription() {
        return description;
    }

    public void setDescription(Object description) {
        this.description = description;
    }

    public Object getInvoice() {
        return invoice;
    }

    public void setInvoice(Object invoice) {
        this.invoice = invoice;
    }

    public Object getLastPaymentError() {
        return lastPaymentError;
    }

    public void setLastPaymentError(Object lastPaymentError) {
        this.lastPaymentError = lastPaymentError;
    }

    public Boolean getLivemode() {
        return livemode;
    }

    public void setLivemode(Boolean livemode) {
        this.livemode = livemode;
    }

    public Metadata getMetadata() {
        return metadata;
    }

    public void setMetadata(Metadata metadata) {
        this.metadata = metadata;
    }

    public Object getNextAction() {
        return nextAction;
    }

    public void setNextAction(Object nextAction) {
        this.nextAction = nextAction;
    }

    public Object getNextSourceAction() {
        return nextSourceAction;
    }

    public void setNextSourceAction(Object nextSourceAction) {
        this.nextSourceAction = nextSourceAction;
    }

    public Object getOnBehalfOf() {
        return onBehalfOf;
    }

    public void setOnBehalfOf(Object onBehalfOf) {
        this.onBehalfOf = onBehalfOf;
    }

    public Object getPaymentMethod() {
        return paymentMethod;
    }

    public void setPaymentMethod(Object paymentMethod) {
        this.paymentMethod = paymentMethod;
    }

    public PaymentMethodOptions getPaymentMethodOptions() {
        return paymentMethodOptions;
    }

    public void setPaymentMethodOptions(PaymentMethodOptions paymentMethodOptions) {
        this.paymentMethodOptions = paymentMethodOptions;
    }

    public List<String> getPaymentMethodTypes() {
        return paymentMethodTypes;
    }

    public void setPaymentMethodTypes(List<String> paymentMethodTypes) {
        this.paymentMethodTypes = paymentMethodTypes;
    }

    public Object getReceiptEmail() {
        return receiptEmail;
    }

    public void setReceiptEmail(Object receiptEmail) {
        this.receiptEmail = receiptEmail;
    }

    public Object getReview() {
        return review;
    }

    public void setReview(Object review) {
        this.review = review;
    }

    public Object getSetupFutureUsage() {
        return setupFutureUsage;
    }

    public void setSetupFutureUsage(Object setupFutureUsage) {
        this.setupFutureUsage = setupFutureUsage;
    }

    public Object getShipping() {
        return shipping;
    }

    public void setShipping(Object shipping) {
        this.shipping = shipping;
    }

    public Object getSource() {
        return source;
    }

    public void setSource(Object source) {
        this.source = source;
    }

    public Object getStatementDescriptor() {
        return statementDescriptor;
    }

    public void setStatementDescriptor(Object statementDescriptor) {
        this.statementDescriptor = statementDescriptor;
    }

    public Object getStatementDescriptorSuffix() {
        return statementDescriptorSuffix;
    }

    public void setStatementDescriptorSuffix(Object statementDescriptorSuffix) {
        this.statementDescriptorSuffix = statementDescriptorSuffix;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public Object getTransferData() {
        return transferData;
    }

    public void setTransferData(Object transferData) {
        this.transferData = transferData;
    }

    public Object getTransferGroup() {
        return transferGroup;
    }

    public void setTransferGroup(Object transferGroup) {
        this.transferGroup = transferGroup;
    }

}
