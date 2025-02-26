import React, { useState, useEffect } from 'react';
import axios from 'axios';
import "@fontsource/inter";

import thumbnail from './Thumbnail.png';

const FileUpload = () => {
    const [selectedFile, setSelectedFile] = useState(null);
    const [uploadStatus, setUploadStatus] = useState('');
    const [availableFiles, setAvailableFiles] = useState([]);
    const [selectedDownloadFile, setSelectedDownloadFile] = useState('');
    const [downloadStatus, setDownloadStatus] = useState('');

    const fetchFiles = async () => {
        try {
            const response = await axios.get('/api/files', {
                auth: {
                    username: 'user',
                    password: 'password',
                },
            });
            console.log('Fetched files:', response.data);
            setAvailableFiles(response.data);
        } catch (error) {
            console.error('Error fetching files:', error);
        }
    };

    useEffect(() => {
        fetchFiles();
    }, []);

    const handleFileChange = (event) => {
        setSelectedFile(event.target.files[0]);
    };

    const handleUpload = async () => {
        if (!selectedFile) {
            alert('Please select a file.');
            return;
        }

        const formData = new FormData();
        formData.append('file', selectedFile);

        try {
            const response = await axios.post('/api/files/upload', formData, {
                auth: {
                    username: 'user',
                    password: 'password',
                },
                headers: {
                    'Content-Type': 'multipart/form-data',
                },
            });
            setUploadStatus('Upload successful: ' + response.data);
            fetchFiles();
        } catch (error) {
            console.error('Upload error:', error);
            setUploadStatus('Upload failed');
        }
    };

    const handleDownload = async () => {
        if (!selectedDownloadFile) {
            alert('Please select a file to download.');
            return;
        }
        try {
            const response = await axios.get(`/api/files/download/${selectedDownloadFile}`, {
                responseType: 'blob',
                auth: {
                    username: 'user',
                    password: 'password',
                },
            });
            const url = window.URL.createObjectURL(new Blob([response.data]));
            const link = document.createElement('a');
            link.href = url;
            link.setAttribute('download', selectedDownloadFile);
            document.body.appendChild(link);
            link.click();
            document.body.removeChild(link);
            setDownloadStatus('Download started.');
        } catch (error) {
            console.error('Download error:', error);
            setDownloadStatus('Download failed.');
        }
    };

    const pageStyle = {
        fontFamily: "'Inter', sans-serif",
        backgroundImage: `url(${thumbnail})`,
        backgroundSize: 'cover',
        backgroundRepeat: 'no-repeat',
        backgroundPosition: 'center center',
        minHeight: '100vh',
        margin: 0,
        padding: 0,
    };

    const containerStyle = {
        display: 'flex',
        justifyContent: 'space-around',
        alignItems: 'center',
        minHeight: '100vh',
        padding: '2rem',
        boxSizing: 'border-box',
    };

    const leftColumnStyle = {
        maxWidth: '45%',
        color: '#fff',
    };

    const headingStyle = {
        fontSize: '7rem',
        fontWeight: 'bold',
        marginBottom: '1rem',
    };

    const subheadingStyle = {
        fontSize: '3.1rem',
        lineHeight: 1.4,
    };

    const rightColumnStyle = {
        display: 'flex',
        flexDirection: 'column',
        gap: '2rem',
        maxWidth: '400px',
        width: '100%',
    };

    const cardStyle = {
        backgroundColor: '#fff',
        color: '#333',
        borderRadius: '8px',
        boxShadow: '0 2px 4px rgba(0,0,0,0.1)',
        padding: '1.5rem',
    };

    const cardTitleStyle = {
        fontSize: '1.25rem',
        marginBottom: '0.5rem',
    };

    const buttonStyle = {
        display: 'inline-block',
        margin: '0.5rem 0',
        backgroundColor: '#333',
        color: '#fff',
        border: 'none',
        borderRadius: '4px',
        padding: '0.5rem 1rem',
        cursor: 'pointer',
    };

    const chooseFileButtonStyle = {
        backgroundColor: '#ccc',
        color: '#000',
        border: '1px solid #000',
        borderRadius: '4px',
        padding: '0.3rem 0.7rem',
        fontSize: '0.8rem',
        cursor: 'pointer',
        display: 'inline-block',
        margin: '0.5rem 0',
    };

    const selectStyle = {
        display: 'block',
        width: '100%',
        margin: '0.5rem 0',
        padding: '0.5rem',
        borderRadius: '8px',
        border: '1px solid #ccc',
        fontFamily: "'Inter', sans-serif",
        fontSize: '0.9rem',
        color: '#333',
        backgroundColor: '#fff',
    };

    return (
        <div style={pageStyle}>
            <div style={containerStyle}>
                <div style={leftColumnStyle}>
                    <div style={headingStyle}>AWS Cloud-based File Sharing</div>
                    <div style={subheadingStyle}>
                        Easily upload and download files via AWS S3
                    </div>
                </div>

                <div style={rightColumnStyle}>
                    {/* Upload Card */}
                    <div style={cardStyle}>
                        <div style={cardTitleStyle}>File Upload</div>
                        <p>Select a file to upload.</p>
                        <input
                            type="file"
                            id="fileInput"
                            onChange={handleFileChange}
                            style={{ display: 'none' }}
                        />
                        <label htmlFor="fileInput" style={chooseFileButtonStyle}>
                            Choose File
                        </label>
                        <span style={{ marginLeft: '1rem' }}>
              {selectedFile ? selectedFile.name : 'No file chosen'}
            </span>
                        <br />

                        <button onClick={handleUpload} style={buttonStyle}>
                            Upload File
                        </button>
                        <p>{uploadStatus}</p>
                    </div>

                    <div style={cardStyle}>
                        <div style={cardTitleStyle}>Download File</div>
                        <p>Select a file to download.</p>
                        <select
                            value={selectedDownloadFile}
                            onChange={(e) => setSelectedDownloadFile(e.target.value)}
                            style={selectStyle}
                        >
                            <option value="">-- Select a file --</option>
                            {availableFiles.map((file) => (
                                <option key={file} value={file}>
                                    {file}
                                </option>
                            ))}
                        </select>
                        <button onClick={handleDownload} style={buttonStyle}>
                            Download File
                        </button>
                        <p>{downloadStatus}</p>
                    </div>
                </div>
            </div>
        </div>
    );
};

export default FileUpload;
