const base = import.meta.env.VITE_API_BASE;

export const getLowestByCategories = () => fetch(`${base}/products/lowest-price`).then(r => r.json());
export const getCheapestBrandSet = () => fetch(`${base}/brands/lowest-price`).then(r => r.json());
export const getCatMinMax = cat => fetch(`${base}/products/category/${cat}`).then(r => r.json());