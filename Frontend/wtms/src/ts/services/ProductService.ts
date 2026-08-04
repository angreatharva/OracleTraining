import { ApiClient } from "./ApiClient";
import { InvestmentProduct, ProductType } from "../models/product";
import { PriceMethod, ProductStatus, RiskCategory } from "../models/enums";

/** Product Service (`PRODUCT-SERVICE`, port 8086) — the investable product catalogue. */
export const ProductService = {
  listProductTypes: (filter?: { status?: ProductStatus; typeName?: string }): Promise<ProductType[]> =>
    ApiClient.get("product", "/api/product-types", filter),

  getProductType: (productTypeId: number): Promise<ProductType> =>
    ApiClient.get("product", `/api/product-types/${productTypeId}`),

  listProducts: (filter?: {
    productTypeId?: number;
    status?: ProductStatus;
    riskCategory?: RiskCategory;
    priceMethod?: PriceMethod;
    productName?: string;
  }): Promise<InvestmentProduct[]> => ApiClient.get("product", "/api/products", filter),

  /** Authoritative quote source — Trading uses this `currentPrice` as the execution price. */
  getProduct: (productId: number): Promise<InvestmentProduct> =>
    ApiClient.get("product", `/api/products/${productId}`)
};
