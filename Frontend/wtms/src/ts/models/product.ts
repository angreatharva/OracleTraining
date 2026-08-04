import { PriceMethod, ProductStatus, RiskCategory } from "./enums";

/** Product Service — `ProductTypeResponse`. */
export interface ProductType {
  productTypeId: number;
  typeCode: string;
  typeName: string;
  description: string | null;
  status: ProductStatus;
  createdAt: string;
  updatedAt: string;
}

/** Product Service — `InvestmentProductResponse`. `currentPrice` is the authoritative quote. */
export interface InvestmentProduct {
  productId: number;
  productTypeId: number;
  productTypeCode: string;
  productName: string;
  basePrice: number;
  currentPrice: number;
  minimumInvestment: number | null;
  riskCategory: RiskCategory;
  priceMethod: PriceMethod;
  tenureMonths: number | null;
  interestRate: number | null;
  issueDate: string | null;
  maturityDate: string | null;
  status: ProductStatus;
  active: boolean;
  createdAt: string;
  updatedAt: string;
}
