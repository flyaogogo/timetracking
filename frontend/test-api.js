const axios = require('axios');

async function testApi() {
  try {
    console.log('Testing monthly stats API...');
    const monthlyResponse = await axios.get('http://localhost:8080/api/dashboard/all-users/monthly-stats?year=2026&month=2');
    console.log('Monthly stats response:', monthlyResponse.data);
    
    console.log('\nTesting yearly stats API...');
    const yearlyResponse = await axios.get('http://localhost:8080/api/dashboard/all-users/yearly-stats?year=2026');
    console.log('Yearly stats response:', yearlyResponse.data);
    
    console.log('\nTesting quarterly stats API...');
    const quarterlyResponse = await axios.get('http://localhost:8080/api/dashboard/all-users/quarterly-stats?year=2026&quarter=1');
    console.log('Quarterly stats response:', quarterlyResponse.data);
  } catch (error) {
    console.error('Error testing API:', error.response ? error.response.data : error.message);
  }
}

testApi();
