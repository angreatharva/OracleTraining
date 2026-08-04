/** Service layer barrel — one module per backend microservice, all routed via the gateway. */
export { ApiClient, ApiError } from "./ApiClient";
export { UserService } from "./UserService";
export { BankService } from "./BankService";
export { ProductService } from "./ProductService";
export { PortfolioService } from "./PortfolioService";
export { TradingService } from "./TradingService";
