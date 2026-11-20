export type GigStatus = 'OPEN' | 'IN_PROGRESS' | 'COMPLETED' | 'CANCELED';

export interface Category {
  id: number;
  name: string;
}

export interface GigRequest {
  title: string;
  description: string;
  categoryId: number;
}

export interface GigResponse {
  id: number;
  title: string;
  description: string;
  status: GigStatus;
  categoryId: number;
  categoryName: string;
  clientId: number;
  clientName: string;
  createdAt: string;
}
