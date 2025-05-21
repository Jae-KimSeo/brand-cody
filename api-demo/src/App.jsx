import { useState } from 'react';
import * as api from './api';
const CATS = ['TOP','OUTER','PANTS','SNEAKERS','BAG','HAT','SOCKS','ACCESSORY'];

export default function App() {
  const [resp, setResp] = useState(null);
  const [cat, setCat] = useState('TOP');

  const Section = ({title, children}) => (
    <div className="mb-6 p-4 border rounded-xl shadow-sm">
      <h2 className="font-bold mb-2">{title}</h2>{children}
    </div>
  );

  return (
    <div className="max-w-2xl mx-auto p-4 space-y-6">
      <h1 className="text-2xl font-semibold">🛍️ Brand-Cody API Playground</h1>

      <Section title="① 카테고리별 최저가">
        <button className="btn" onClick={() => api.getLowestByCategories().then(setResp)}>Run</button>
      </Section>

      <Section title="② 단일 브랜드 최저 세트">
        <button className="btn" onClick={() => api.getCheapestBrandSet().then(setResp)}>Run</button>
      </Section>

      <Section title="③ 특정 카테고리 최고/최저">
        <select value={cat} onChange={e=>setCat(e.target.value)} className="border px-2 py-1 mr-2">
          {CATS.map(c=> <option key={c}>{c}</option>)}
        </select>
        <button className="btn" onClick={() => api.getCatMinMax(cat).then(setResp)}>Run</button>
      </Section>

      {resp && (
        <pre className="bg-gray-100 p-3 rounded overflow-x-auto text-sm">
          {JSON.stringify(resp, null, 2)}
        </pre>
      )}
    </div>
  );
}