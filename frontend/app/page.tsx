"use client";

import { useState, useEffect } from "react";

export default function Home() {
  const [key, setKey] = useState("");
  const [value, setValue] = useState("");
  const [searchKey, setSearchKey] = useState("");
  const [searchResult, setSearchResult] = useState<string | null>(null);
  const [stats, setStats] = useState<{ memTableSize: number; sstableCount: number } | null>(null);
  const [loading, setLoading] = useState(false);

  const API_URL = process.env.NEXT_PUBLIC_API_URL || "http://localhost:8080";

  const fetchStats = async () => {
    try {
      const res = await fetch(`${API_URL}/api/stats`);
      const data = await res.json();
      setStats(data);
    } catch (error) {
      console.error("Failed to fetch stats", error);
    }
  };

  useEffect(() => {
    fetchStats();
    const interval = setInterval(fetchStats, 2000);
    return () => clearInterval(interval);
  }, []);

  const handleSet = async () => {
    setLoading(true);
    try {
      await fetch(`${API_URL}/api/set`, {
        method: "POST",
        headers: { "Content-Type": "application/json" },
        body: JSON.stringify({ key, value }),
      });
      setKey("");
      setValue("");
      fetchStats();
    } catch (error) {
      alert("Failed to set value");
    } finally {
      setLoading(false);
    }
  };

  const handleGet = async () => {
    try {
      const res = await fetch(`${API_URL}/api/get?key=${searchKey}`);
      const data = await res.text();
      setSearchResult(data);
    } catch (error) {
      setSearchResult("Error fetching key");
    }
  };

  return (
    <div className="min-h-screen bg-gray-900 text-white p-10 font-sans">
      <div className="max-w-4xl mx-auto">
        <h1 className="text-4xl font-bold mb-8 text-transparent bg-clip-text bg-gradient-to-r from-blue-400 to-purple-500">
          Java LSM Database
        </h1>

        {/* Stats Panel */}
        <div className="grid grid-cols-2 gap-6 mb-10">
          <div className="bg-gray-800 p-6 rounded-xl border border-gray-700 shadow-lg">
            <h2 className="text-gray-400 text-sm uppercase tracking-wider mb-2">MemTable Size</h2>
            <p className="text-3xl font-mono text-green-400">
              {stats ? `${stats.memTableSize} bytes` : "Loading..."}
            </p>
          </div>
          <div className="bg-gray-800 p-6 rounded-xl border border-gray-700 shadow-lg">
            <h2 className="text-gray-400 text-sm uppercase tracking-wider mb-2">SSTables on Disk</h2>
            <p className="text-3xl font-mono text-yellow-400">
              {stats ? stats.sstableCount : "Loading..."}
            </p>
          </div>
        </div>

        <div className="grid grid-cols-1 md:grid-cols-2 gap-10">
          {/* Write Section */}
          <div className="bg-gray-800 p-8 rounded-xl border border-gray-700">
            <h2 className="text-2xl font-semibold mb-6 flex items-center">
              <span className="bg-blue-500 w-2 h-8 mr-3 rounded-full"></span>
              Write Data
            </h2>
            <div className="space-y-4">
              <div>
                <label className="block text-sm text-gray-400 mb-1">Key</label>
                <input
                  type="text"
                  value={key}
                  onChange={(e) => setKey(e.target.value)}
                  className="w-full bg-gray-900 border border-gray-600 rounded-lg p-3 focus:border-blue-500 outline-none transition"
                  placeholder="user:123"
                />
              </div>
              <div>
                <label className="block text-sm text-gray-400 mb-1">Value</label>
                <input
                  type="text"
                  value={value}
                  onChange={(e) => setValue(e.target.value)}
                  className="w-full bg-gray-900 border border-gray-600 rounded-lg p-3 focus:border-blue-500 outline-none transition"
                  placeholder="Alice Smith"
                />
              </div>
              <button
                onClick={handleSet}
                disabled={loading || !key || !value}
                className="w-full bg-blue-600 hover:bg-blue-500 text-white font-bold py-3 rounded-lg transition disabled:opacity-50 disabled:cursor-not-allowed"
              >
                {loading ? "Writing..." : "Set Key"}
              </button>
            </div>
          </div>

          {/* Read Section */}
          <div className="bg-gray-800 p-8 rounded-xl border border-gray-700">
            <h2 className="text-2xl font-semibold mb-6 flex items-center">
              <span className="bg-purple-500 w-2 h-8 mr-3 rounded-full"></span>
              Read Data
            </h2>
            <div className="space-y-4">
              <div className="flex gap-2">
                <input
                  type="text"
                  value={searchKey}
                  onChange={(e) => setSearchKey(e.target.value)}
                  className="flex-1 bg-gray-900 border border-gray-600 rounded-lg p-3 focus:border-purple-500 outline-none transition"
                  placeholder="Search key..."
                />
                <button
                  onClick={handleGet}
                  className="bg-purple-600 hover:bg-purple-500 text-white font-bold px-6 rounded-lg transition"
                >
                  Get
                </button>
              </div>

              {searchResult && (
                <div className="mt-6 p-4 bg-gray-900 rounded-lg border border-gray-600">
                  <p className="text-sm text-gray-400 mb-1">Result:</p>
                  <p className={`text-lg font-mono ${searchResult === "NOT_FOUND" ? "text-red-400" : "text-green-400"}`}>
                    {searchResult}
                  </p>
                </div>
              )}
            </div>
          </div>
        </div>
      </div>
    </div>
  );
}
